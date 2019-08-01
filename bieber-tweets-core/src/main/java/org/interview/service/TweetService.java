package org.interview.service;

import lombok.extern.slf4j.Slf4j;
import org.interview.model.Tweet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Collects and processes tweets in reactive manner.
 * */
@Slf4j
@Service
public class TweetService {

    private static final DateTimeFormatter TWITTER_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy");

    private final String hashtag;
    private final int maxItems;
    private final Duration maxDurationInSeconds;
    private final TweeterStreamingClient tweeterStreamingClient;

    public TweetService(
            @Value("${service.hashtag}") final String hashtag,
            @Value("${service.max-items}") final int maxItems,
            @Value("${service.max-duration-in-seconds}") final int maxDurationInSeconds,
            final TweeterStreamingClient tweeterStreamingClient
    ) {
        this.hashtag = hashtag;
        this.maxItems = maxItems;
        this.maxDurationInSeconds = Duration.ofSeconds(maxDurationInSeconds);
        this.tweeterStreamingClient = tweeterStreamingClient;
    }

    /**
     * Collects tweets streamed by twitter client:
     * - collects not more tweets than maxItems
     * - collects tweets for not longer than maxDurationInSeconds
     * - sorts result tweets chronologically and grouped by users
     *
     * @return mono representing collected items as a sorted list.
     * */
    public Mono<List<Tweet>> latestTweets() {
        log.info("Collecting tweets for hashtag {} for next {} seconds", hashtag, maxDurationInSeconds.getSeconds());
        return tweeterStreamingClient.openStream(hashtag)
                .limitRequest(maxItems)
                .take(maxDurationInSeconds)
                .doOnError(TimeoutException.class, e -> log.error("Failed to retrieve", e))
                .collectSortedList(chronologicallyGroupedByUser())
                .doOnError(throwable -> log.error("Failed to process tweets", throwable));
    }

    /**
     * This comparator does:
     * - grouping tweets by users
     * - sorting tweets first by user's created_at field
     * - then sorting tweets by their created_at field for each user
     * */
    private Comparator<Tweet> chronologicallyGroupedByUser() {
        return Comparator
                .comparing(
                        Tweet::getUser,
                        Comparator.comparing(
                                user -> LocalDateTime.parse(user.getCreatedAt(), TWITTER_DATE_TIME_FORMATTER)
                        )
                ).thenComparing(
                        tweet -> LocalDateTime.parse(tweet.getCreatedAt(), TWITTER_DATE_TIME_FORMATTER)
                );
    }
}