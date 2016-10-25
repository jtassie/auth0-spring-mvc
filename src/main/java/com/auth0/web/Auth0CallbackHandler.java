package com.auth0.web;

import com.auth0.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * The Servlet endpoint used as the callback handler in the Oauth2
 * authorization code grant flow. This servlet is called back via a
 * redirect from Auth0 (IdP) post authentication supplying an authorization code
 *
 * Using inheritance or composition leverage this callback handler from a Controller
 *
 * Example usage - Simply extend this class and define Controller in subclass
 * <pre><code>
package com.auth0.example;

import com.auth0.web.Auth0CallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

{@literal @}Controller
public class CallbackController extends Auth0CallbackHandler {

    {@literal @}RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        super.handle(req, res);
    }
}
 * </code></pre>
 *
 */
@Component
public class Auth0CallbackHandler {

    protected String redirectOnSuccess;
    protected String redirectOnFail;
    protected Auth0Config auth0Config;
    protected Auth0Client auth0Client;

    @Autowired
    protected void setAuth0Client(final Auth0Client auth0Client) {
        this.auth0Client = auth0Client;
    }

    @Autowired
    protected void setAuth0Config(final Auth0Config auth0Config) {
        this.auth0Config = auth0Config;
        this.redirectOnSuccess = auth0Config.getLoginRedirectOnSuccess();
        this.redirectOnFail = auth0Config.getLoginRedirectOnFail();
    }

    /**
     * Entrypoint for http request
     *
     * 1). Responsible for validating the request and ensuring
     * the nonce value in session storage matches the nonce value passed to this endpoint.
     * 2). Exchanging the authorization code received with this http request for tokens
     * 3). Getting user profile information using id token
     * 4). Storing both tokens and user profile information into session storage
     * 5). Clearing the stored nonce value out of state storage
     * 6). Handling success and any failure outcomes
     */
    public void handle(final HttpServletRequest req, final HttpServletResponse res)
            throws IOException, ServletException {
        try {
            if (isValidRequest(req)) {
                final Tokens tokens = fetchTokens(req);
                final Auth0User auth0User = auth0Client.getUserProfile(tokens);
                store(tokens, auth0User, req);
                NonceUtils.removeNonceFromStorage(req);
                onSuccess(req, res);
            } else {
                onFailure(req, res, new IllegalStateException("Invalid state or error"));
            }
        } catch (RuntimeException ex) {
            onFailure(req, res, ex);
        }
    }

    /**
     * Actions / navigation to take when a request is deemed successful by this callback handler
     */
    protected void onSuccess(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        res.sendRedirect(req.getContextPath() + redirectOnSuccess);
    }

    /**
     * Actions / navigation to take when a request is deemed unsuccessful by this callback handler
     */
    protected void onFailure(final HttpServletRequest req, final HttpServletResponse res,
                             final Exception e) throws ServletException, IOException {
        e.printStackTrace();
        final String redirectOnFailLocation = req.getContextPath() + redirectOnFail;
        res.sendRedirect(redirectOnFailLocation);
    }

    /**
     * Store tokens and auth0User
     *
     * @param tokens the tokens
     * @param user the user profile
     * @param req the http servlet request
     */
    protected void store(final Tokens tokens, final Auth0User user, final HttpServletRequest req) {
        SessionUtils.setTokens(req, tokens);
        SessionUtils.setAuth0User(req, user);
    }

    /**
     * Get tokens for this request
     *
     * @param req the http servlet request
     * @return the tokens associated with the authentication request
     * @throws IOException
     */
    protected Tokens fetchTokens(final HttpServletRequest req) {
        final String authorizationCode = req.getParameter("code");
        final String redirectUri = req.getRequestURL().toString();
        return auth0Client.getTokens(authorizationCode, redirectUri);
    }

    /**
     * Indicates whether the request is deemed valid
     *
     * @param req the http servlet request
     * @return boolean whether this request is deemed valid
     * @throws IOException
     */
    protected boolean isValidRequest(final HttpServletRequest req) throws IOException {
        return !hasError(req) && isValidState(req);
    }

    /**
     * Checks for the presence of an error in the http servlet request params
     *
     * @param req the http servlet request
     * @return boolean whether this http servlet request indicates an error was present
     */
    protected boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

    /**
     * Indicates whether the nonce value in storage matches the nonce value passed
     * with the http servlet request
     *
     * @param req the http servlet request
     * @return boolean whether nonce value in storage matches the nonce value in the http request
     */
    protected boolean isValidState(final HttpServletRequest req) {
        final String stateFromRequest = req.getParameter("state");
        return NonceUtils.matchesNonceInStorage(req, stateFromRequest);
    }

}
