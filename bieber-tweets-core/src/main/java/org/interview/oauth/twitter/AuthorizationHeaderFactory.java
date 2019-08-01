package org.interview.oauth.twitter;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;

/**
 * Factory generating authorization header value to access Twitter API.
 * */
@Component
public class AuthorizationHeaderFactory {

    private final OAuthKeyVault oAuthKeyVault;

    public AuthorizationHeaderFactory(final OAuthKeyVault oAuthKeyVault) {
        this.oAuthKeyVault = oAuthKeyVault;
    }

    /**
     * Generates value for authorization header to access Twitter API.
     *
     * @return authorization header value.
     * */
    public String headerFor(final String requestMethod, final String requestUrl) {
        final OAuthParameters oAuthParameters = new OAuthParameters();

        final OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = oAuthKeyVault.getClientSharedSecret();
        signer.tokenSharedSecret = oAuthKeyVault.getTokenSharedSecret();
        oAuthParameters.signer = signer;

        oAuthParameters.callback = oAuthKeyVault.getCallback();
        oAuthParameters.consumerKey = oAuthKeyVault.getConsumerKey();
        oAuthParameters.realm = oAuthKeyVault.getRealm();
        oAuthParameters.signature = oAuthKeyVault.getSignature();
        oAuthParameters.signatureMethod = oAuthKeyVault.getSignatureMethod();
        oAuthParameters.token = oAuthKeyVault.getToken();
        oAuthParameters.verifier = oAuthKeyVault.getVerifier();
        oAuthParameters.version = oAuthKeyVault.getVersion();

        oAuthParameters.computeNonce();
        oAuthParameters.computeTimestamp();

        try {
            oAuthParameters.computeSignature(requestMethod, new GenericUrl(requestUrl));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to sign oath request", e);
        }

        return oAuthParameters.getAuthorizationHeader();
    }
}