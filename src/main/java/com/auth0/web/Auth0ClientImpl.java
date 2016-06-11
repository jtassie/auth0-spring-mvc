package com.auth0.web;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Credentials;
import com.auth0.authentication.result.UserProfile;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Auth0ClientImpl implements Auth0Client {

    protected Auth0Config auth0Config;
    protected AuthenticationAPIClient authenticationAPIClient;

    @Autowired
    public Auth0ClientImpl(final Auth0Config auth0Config) {
        Validate.notNull(auth0Config);
        this.auth0Config = auth0Config;
        final Auth0 auth0 = new Auth0(auth0Config.getClientId(), auth0Config.getClientSecret(), auth0Config.getDomain());
        authenticationAPIClient = new AuthenticationAPIClient(auth0);
    }

    @Override
    public Tokens getTokens(final String authorizationCode, final String redirectUri) {
        Validate.notNull(authorizationCode);
        Validate.notNull(redirectUri);
        final Credentials creds = authenticationAPIClient
                .token(authorizationCode, redirectUri)
                .setClientSecret(auth0Config.getClientSecret()).execute();
        return new Tokens(creds.getIdToken(), creds.getAccessToken(), creds.getType(), creds.getRefreshToken());
    }

    @Override
    public UserProfile getUserProfile(final Tokens tokens) {
        Validate.notNull(tokens);
        final String idToken = tokens.getIdToken();
        return authenticationAPIClient.tokenInfo(idToken).execute();
    }

}
