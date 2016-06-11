package com.auth0.web;

import com.auth0.authentication.result.UserProfile;
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
 *
 * Example usage - Create a Controller, and use composition, simply pass your action requests on to the handle(req, res)
 * method of this delegate class.
 *
 *
 * package com.auth0.example;
 *
 * import Auth0CallbackHandler;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.stereotype.Controller;
 * import org.springframework.web.bind.annotation.RequestMapping;
 * import org.springframework.web.bind.annotation.RequestMethod;
 *
 * import javax.servlet.ServletException;
 * import javax.servlet.http.HttpServletRequest;
 * import javax.servlet.http.HttpServletResponse;
 * import java.io.IOException;
 *
 * @Controller
 * public class CallbackController {
 *
 *    @Autowired
 *    protected Auth0CallbackHandler callback;
 *
 *    @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
 *    protected void callback(final HttpServletRequest req, final HttpServletResponse res)
 *                                                    throws ServletException, IOException {
 *        callback.handle(req, res);
 *    }
 * }
 *
 */
@Component
public class Auth0CallbackHandler {

    protected String redirectOnSuccess;
    protected String redirectOnFail;
    protected Auth0Config appConfig;
    protected Auth0Client auth0Client;

    @Autowired
    protected void setAuth0Client(final Auth0Client auth0Client) {
        this.auth0Client = auth0Client;
    }

    @Autowired
    protected void setAppConfig(Auth0Config appConfig) {
        this.appConfig = appConfig;
        this.redirectOnSuccess = appConfig.getLoginRedirectOnSuccess();
        this.redirectOnFail = appConfig.getLoginRedirectOnFail();
    }

    /**
     * Entry point
     */
    public void handle(final HttpServletRequest req, final HttpServletResponse res)
            throws IOException, ServletException {
        if (isValidRequest(req)) {
            try {
                final Tokens tokens = fetchTokens(req);
                final UserProfile userProfile = auth0Client.getUserProfile(tokens);
                store(tokens, new Auth0User(userProfile), req);
                NonceUtils.removeNonceFromStorage(req);
                onSuccess(req, res);
            } catch (IllegalArgumentException ex) {
                onFailure(req, res, ex);
            } catch (IllegalStateException ex) {
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
                             Exception ex) throws ServletException, IOException {
        ex.printStackTrace();
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
        if (hasError(req)) {
            return false;
        }
        final String stateFromRequest = req.getParameter("state");
        return NonceUtils.matchesNonceInStorage(req, stateFromRequest);
    }

    protected static boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

}
