package org.interview.oauth.twitter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Immutable storage containing fields required to generate authorization header for Twitter API.
 * */
@Getter
@AllArgsConstructor
public class OAuthKeyVault {

    private final String clientSharedSecret;
    private final String tokenSharedSecret;
    private final String callback;
    private final String consumerKey;
    private final String nonce;
    private final String realm;
    private final String signature;
    private final String signatureMethod;
    private final String timestamp;
    private final String token;
    private final String verifier;
    private final String version;

}