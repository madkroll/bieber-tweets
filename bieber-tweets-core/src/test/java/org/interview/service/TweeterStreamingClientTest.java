package org.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.interview.model.Tweet;
import org.interview.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TweeterStreamingClientTest {

    private static final String SAMPLE_HASHTAG = "any-hash-tag";

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

    private MockWebServer mockWebServer;

    @Before
    public void setup() {
        mockWebServer = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldStreamIncomingTweets() {
        // given
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonWriter.apply(Arrays.asList(TWEET_FIRST, TWEET_SECOND)))
        );

        // when
        final List<Tweet> resultTweets =
                new TweeterStreamingClient(WebClient.builder()
                        .baseUrl(mockWebServer.url("/").toString())).openStream(SAMPLE_HASHTAG)
                        .collectList()
                        .block();

        // then
        assertThat(resultTweets)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(TWEET_FIRST, TWEET_SECOND);
    }

    @Test
    public void shouldFailIfReceivedErrorResponse() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        // then
        assertThatThrownBy(() -> {
            new TweeterStreamingClient(WebClient.builder()
                    .baseUrl(mockWebServer.url("/").toString())).openStream(SAMPLE_HASHTAG)
                    .collectList()
                    .block();
        }).isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Unable to stream for hashtag: %s. Status code: %d",
                        SAMPLE_HASHTAG,
                        HttpStatus.BAD_REQUEST.value()
                );
    }

    private Function<Object, String> jsonWriter = (object) -> {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize to JSON");
        }
    };
}
