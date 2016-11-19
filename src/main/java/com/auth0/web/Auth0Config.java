package com.auth0.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Holds the default configuration for the library
 */
@Component
@Configuration
public class Auth0Config {

    /**
     * This is your auth0 domain (tenant you have created when registering with auth0 - account name)
     */
    @Value(value = "${auth0.domain}")
    protected String domain;

    /**
     * This is the issuer of the JWT Token (typically full URL of your auth0 tenant account
     * eg. https://{tenant_name}.auth0.com/
     */
    @Value(value = "${auth0.issuer}")
    protected String issuer;

    /**
     * This is the client id of your auth0 application (see Settings page on auth0 dashboard)
     */
    @Value(value = "${auth0.clientId}")
    protected String clientId;

    /**
     * This is the client secret of your auth0 application (see Settings page on auth0 dashboard)
     */
    @Value(value = "${auth0.clientSecret}")
    protected String clientSecret;

    /**
     * This is the page / view that users of your site are redirected to on logout. Should start with `/`
     */
    @Value(value = "${auth0.onLogoutRedirectTo}")
    protected String onLogoutRedirectTo;

    /**
     * This is the landing page URL context path for a successful authentication. Should start with `/`
     */
    @Value(value = "${auth0.loginRedirectOnSuccess}")
    protected String loginRedirectOnSuccess;

    /**
     * This is the URL context path for the page to redirect to upon failure. Should start with `/`
     */
    @Value(value = "${auth0.loginRedirectOnFail}")
    protected String loginRedirectOnFail;

    /**
     * This is the URL context path for the login callback endpoint. Should start with `/`
     */
    @Value(value = "${auth0.loginCallback}")
    protected String loginCallback;

    /**
     * This is the URL pattern to secure a URL endpoint. Should start with `/`
     */
    @Value(value = "${auth0.securedRoute}")
    protected String securedRoute;

    /**
     * This is a boolean value indicating whether the Secret used to verify the JWT is base64 encoded. Default is `true`
     */
    @Value(value = "${auth0.base64EncodedSecret:true}")
    protected boolean base64EncodedSecret;

    /**
     * This is signing algorithm to verify signed JWT token. Use `HS256` or `RS256`.
     * Default to HS256 for backwards compatibility
     */
    @Value(value = "${auth0.signingAlgorithm:HS256}")
    protected String signingAlgorithm;

    /**
     * This is the path location to the public key stored locally on disk / inside your application War file WEB-INF directory.
     * Should always be set when using `RS256`.
     * Default to empty string as HS256 is default
     */
    @Value(value = "${auth0.publicKeyPath:}")
    protected String publicKeyPath;


    public String getDomain() {
        return domain;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getOnLogoutRedirectTo() {
        return onLogoutRedirectTo;
    }

    public String getLoginRedirectOnSuccess() {
        return loginRedirectOnSuccess;
    }

    public String getLoginRedirectOnFail() {
        return loginRedirectOnFail;
    }

    public String getLoginCallback() {
        return loginCallback;
    }

    public String getSecuredRoute() {
        return securedRoute;
    }

    public boolean isBase64EncodedSecret() {
        return base64EncodedSecret;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

}
