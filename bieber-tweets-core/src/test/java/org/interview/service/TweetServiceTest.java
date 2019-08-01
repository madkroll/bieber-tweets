package org.interview.service;

import org.assertj.core.util.Lists;
import org.interview.model.Tweet;
import org.interview.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class TweetServiceTest {

    private static final String SAMPLE_HASHTAG = "sample-hash-tag";
    private static final int SECONDS_TO_WAIT = 30;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy");

    private static final Duration TWEET_INTERVAL_IN_SECONDS = Duration.ofSeconds(5);

    private static final List<Tweet> SOURCE_TWEETS = buildSourceTweets();

    @Mock
    private TweeterStreamingClient tweeterStreamingClient;

    @Test
    public void shouldReturnOrderedMaxRequestedItemsBeforeMaxDurationReached() {
        assertStreamedResults(
                // items to request
                3,
                // expected first limited sorted tweets
                Lists.newArrayList(
                        SOURCE_TWEETS.get(0),
                        SOURCE_TWEETS.get(2),
                        SOURCE_TWEETS.get(1)
                )
        );
    }

    @Test
    public void shouldReturnAllAvailableOrderedItemsAndReachMaxDuration() {
        assertStreamedResults(
                // items to request
                10,
                // expected all sorted tweets
                Lists.newArrayList(
                        SOURCE_TWEETS.get(0),
                        SOURCE_TWEETS.get(2),
                        SOURCE_TWEETS.get(1),
                        SOURCE_TWEETS.get(3)
                )
        );
    }

    private void assertStreamedResults(
            final int itemsToRequest,
            final List<Tweet> expectedSortedTweets
    ) {
        // then
        StepVerifier
                .withVirtualTime(() -> {
                            given(tweeterStreamingClient.openStream(SAMPLE_HASHTAG))
                                    .willReturn(
                                            Flux.fromIterable(SOURCE_TWEETS)
                                                    .delayElements(TWEET_INTERVAL_IN_SECONDS)
                                    );

                            return new TweetService(SAMPLE_HASHTAG, itemsToRequest, SECONDS_TO_WAIT, tweeterStreamingClient)
                                    .latestTweets();
                        }
                )
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(SECONDS_TO_WAIT))
                .assertNext(
                        tweets -> assertThat(tweets)
                                .usingRecursiveFieldByFieldElementComparator()
                                .containsExactlyElementsOf(expectedSortedTweets)
                )
                .verifyComplete();
    }

    private static List<Tweet> buildSourceTweets() {
        final User oldUser = new User("old_user", "Mon Dec 29 17:52:08 +0000 2014", "name", "screen_name");
        final User youngUser = new User("young_user", "Fri May 17 23:16:29 +0000 2019", "name", "screen_name");
        final ZonedDateTime firstTweetTime = ZonedDateTime.parse("Sat May 18 00:00:00 +0000 2019", DATE_TIME_FORMATTER);

        return Lists.newArrayList(
                new Tweet("id_" + 0, firstTweetTime.format(DATE_TIME_FORMATTER), "first", oldUser),
                new Tweet("id_" + 1, firstTweetTime.plusSeconds(5).format(DATE_TIME_FORMATTER), "second", youngUser),
                new Tweet("id_" + 2, firstTweetTime.plusSeconds(10).format(DATE_TIME_FORMATTER), "third", oldUser),
                new Tweet("id_" + 3, firstTweetTime.plusSeconds(15).format(DATE_TIME_FORMATTER), "forth", youngUser)
        );
    }
}
