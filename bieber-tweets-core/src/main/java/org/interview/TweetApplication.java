package org.interview;

import org.interview.model.Tweet;
import org.interview.service.JsonWriter;
import org.interview.service.TweetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.PrintStream;
import java.util.List;

@SpringBootApplication
public class TweetApplication implements CommandLineRunner {

    private final TweetService tweetService;
    private final PrintStream printStream;
    private final JsonWriter jsonWriter;

    public TweetApplication(final TweetService tweetService, final PrintStream printStream, final JsonWriter jsonWriter) {
        this.tweetService = tweetService;
        this.printStream = printStream;
        this.jsonWriter = jsonWriter;
    }

    public static void main(String[] args) {
        SpringApplication.run(TweetApplication.class, args);
    }

    @Override
    public void run(final String... args) {
        final List<Tweet> latestTweetsFormatted =
                tweetService.latestTweets().blockOptional()
                        .orElseThrow(() -> new IllegalStateException("Failed to read tweets."));

        printStream.println(jsonWriter.writeToString(latestTweetsFormatted));
    }
}
