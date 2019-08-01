package org.interview.service;

import lombok.extern.slf4j.Slf4j;
import org.interview.model.Tweet;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;

/**
 * Accesses Twitter API using authorized web client builder and streams received response in reactive manner.
 * */
@Slf4j
@Component
public class TweeterStreamingClient {

    private final Builder authorizedWebClientBuilder;

    public TweeterStreamingClient(
            final Builder authorizedWebClientBuilder
    ) {
        this.authorizedWebClientBuilder = authorizedWebClientBuilder;
    }

    public Flux<Tweet> openStream(final String hashtagToStreamFor) {
        final String requestPath = "?track=" + hashtagToStreamFor;

        return authorizedWebClientBuilder.build()
                .post()
                .uri(requestPath)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .onStatus(
                        HttpStatus::isError, clientResponse -> {
                            final String errorMessage = String.format(
                                    "Unable to stream for hashtag: %s. Status code: %d",
                                    hashtagToStreamFor,
                                    clientResponse.statusCode().value()
                            );
                            log.error(errorMessage);
                            throw new IllegalStateException(errorMessage);
                        }
                )
                .bodyToFlux(Tweet.class);
    }
}
