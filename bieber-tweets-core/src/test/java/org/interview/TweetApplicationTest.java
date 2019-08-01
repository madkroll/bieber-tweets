package org.interview;

import org.assertj.core.util.Lists;
import org.interview.model.Tweet;
import org.interview.model.User;
import org.interview.service.JsonWriter;
import org.interview.service.TweetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TweetApplicationTest {

    private static final String PRINTED_OUTPUT = "tweets-as-a-json-string";

    private static final Tweet TWEET_FIRST =
            new Tweet(
                    "tweet-id-first", "tweet-created-at-first", "tweet-text-first",
                    new User("user-id-first", "user-created-at-first", "user-name-first", "user-screenName-first")
            );

    private static final Tweet TWEET_SECOND =
            new Tweet(
                    "tweet-id-second", "tweet-created-at-second", "tweet-text-second",
                    new User("user-id-second", "user-created-at-second", "user-name-second", "user-screenName-second")
            );

    @Mock
    private TweetService tweetService;

    @Mock
    private PrintStream printStream;

    @Mock
    private JsonWriter jsonWriter;

    @Test
    public void shouldPrintResultTweetsAsJson() {
        // given
        final List<Tweet> tweets = Lists.newArrayList(TWEET_FIRST, TWEET_SECOND);
        given(tweetService.latestTweets()).willReturn(Mono.just(tweets));
        given(jsonWriter.writeToString(tweets)).willReturn(PRINTED_OUTPUT);

        // when
        new TweetApplication(tweetService, printStream, jsonWriter).run();

        // then
        verify(printStream).println(PRINTED_OUTPUT);
    }

    @Test
    public void shouldFailIfNoTweets() {
        // given
        given(tweetService.latestTweets()).willReturn(Mono.empty());

        assertThatThrownBy(() -> new TweetApplication(tweetService, printStream, jsonWriter).run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read tweets.");

        verifyZeroInteractions(printStream);
    }
}
