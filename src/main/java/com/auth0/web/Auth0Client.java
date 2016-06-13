package com.auth0.web;

public interface Auth0Client {

    public Tokens getTokens(String authorizationCode, String redirectUri);

    public Auth0User getUserProfile(Tokens tokens);

}
