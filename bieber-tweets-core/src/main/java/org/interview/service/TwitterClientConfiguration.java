package org.interview.service;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.HttpRequestFactory;
import org.interview.oauth.twitter.AuthorizationHeaderFactory;
import org.interview.oauth.twitter.OAuthKeyVault;
import org.interview.oauth.twitter.TwitterAuthenticationException;
import org.interview.oauth.twitter.TwitterAuthenticator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintStream;

/**
 * Configuration bean for Twitter Client.
 * */
@Configuration
public class TwitterClientConfiguration {

    @Bean
    public PrintStream printStream() {
        return System.out;
    }

    /**
     * Defines immutable thread-safe instance for WebClient.Builder, authorized to access Twitter API.
     * */
    @Bean
    public WebClient.Builder authorizedWebClientBuilder(
            @Value("${service.hashtag}") final String hashtag,
            @Value("${twitter.stream-api-url}") final String apiUrl,
            final AuthorizationHeaderFactory authorizationHeaderFactory
    ) {
        final String targetUrl = apiUrl + "?track=" + hashtag;

        return WebClient.builder()
                .baseUrl(apiUrl)
                .filter((currentRequest, next) ->
                        next.exchange(
                                ClientRequest.from(currentRequest)
                                        .header(
                                                HttpHeaders.AUTHORIZATION,
                                                authorizationHeaderFactory.headerFor(HttpMethod.POST.name(), targetUrl)
                                        ).build()
                        )
                );
    }

    /**
     * Defines key vault bean containing all data required to authorize requests to access Twitter API.
     * Underneath, uses Google OAuth client to obtain oath session and extract authorization signature.
     * */
    @Bean
    public OAuthKeyVault twitterTemplate(
            final PrintStream printStream,
            @Value("${twitter.consumer-key}") final String consumerKey,
            @Value("${twitter.consumer-secret}") final String consumerSecret
    ) {
        try {
            final HttpRequestFactory requestFactory =
                    new TwitterAuthenticator(printStream, consumerKey, consumerSecret).getAuthorizedHttpRequestFactory();

            final OAuthParameters initializer = (OAuthParameters) requestFactory.getInitializer();

            final OAuthHmacSigner signer = (OAuthHmacSigner) initializer.signer;

            return new OAuthKeyVault(
                    signer.clientSharedSecret,
                    signer.tokenSharedSecret,
                    initializer.callback,
                    initializer.consumerKey,
                    initializer.nonce,
                    initializer.realm,
                    initializer.signature,
                    initializer.signatureMethod,
                    initializer.timestamp,
                    initializer.token,
                    initializer.verifier,
                    initializer.version
            );
        } catch (TwitterAuthenticationException e) {
            throw new IllegalStateException("Failed to initialize Twitter Client", e);
        }
    }
}
