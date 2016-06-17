package com.auth0.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 *
 * Using inheritance or composition leverage this callback handler from a Controller
 *
 * Example usage - Simply extend this class and define Controller in subclass
 *
 *
 *  package com.auth0.example;
 *
 * import com.auth0.web.Auth0CallbackHandler;
 * import org.springframework.stereotype.Controller;
 * import org.springframework.web.bind.annotation.RequestMapping;
 * import org.springframework.web.bind.annotation.RequestMethod;
 *
 * import javax.servlet.ServletException;
 * import javax.servlet.http.HttpServletRequest;
 * import javax.servlet.http.HttpServletResponse;
 * import java.io.IOException;
 *
 *  @Controller
 *  public class CallbackController extends Auth0CallbackHandler {
 *
 *      @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
 *      protected void callback(final HttpServletRequest req, final HttpServletResponse res)
 *                                                      throws ServletException, IOException {
 *          super.handle(req, res);
 *      }
 *  }
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
     * Entry point
     */
    public void handle(final HttpServletRequest req, final HttpServletResponse res)
            throws IOException, ServletException {
        if (isValidRequest(req)) {
            try {
                final Tokens tokens = fetchTokens(req);
                final Auth0User auth0User = auth0Client.getUserProfile(tokens);
                store(tokens, auth0User, req);
                NonceUtils.removeNonceFromStorage(req);
                onSuccess(req, res);
            } catch (RuntimeException ex) {
                onFailure(req, res, ex);
            }
        } else {
            onFailure(req, res, new IllegalStateException("Invalid state or error"));
        }
    }

    protected void onSuccess(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        res.sendRedirect(req.getContextPath() + redirectOnSuccess);
    }

    protected void onFailure(final HttpServletRequest req, final HttpServletResponse res,
                             final Exception e) throws ServletException, IOException {
        e.printStackTrace();
        final String redirectOnFailLocation = req.getContextPath() + redirectOnFail;
        res.sendRedirect(redirectOnFailLocation);
    }

    protected void store(final Tokens tokens, final Auth0User user, final HttpServletRequest req) {
        SessionUtils.setTokens(req, tokens);
        SessionUtils.setAuth0User(req, user);
    }

    protected Tokens fetchTokens(final HttpServletRequest req) {
        final String authorizationCode = req.getParameter("code");
        final String redirectUri = req.getRequestURL().toString();
        return auth0Client.getTokens(authorizationCode, redirectUri);
    }

    protected boolean isValidRequest(final HttpServletRequest req) throws IOException {
        return !hasError(req) && isValidState(req);
    }

    protected boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

    protected boolean isValidState(final HttpServletRequest req) {
        final String stateFromRequest = req.getParameter("state");
        return NonceUtils.matchesNonceInStorage(req, stateFromRequest);
    }

}
