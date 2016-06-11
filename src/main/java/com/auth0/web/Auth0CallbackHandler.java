package com.auth0.web;

import com.auth0.Auth0;
import com.auth0.Auth0Exception;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Credentials;
import com.auth0.authentication.result.UserProfile;
import org.apache.commons.lang3.Validate;
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
    protected AuthenticationAPIClient authenticationAPIClient;

    @Autowired
    protected void setAppConfig(Auth0Config appConfig) {
        this.appConfig = appConfig;
        this.redirectOnSuccess = appConfig.getLoginRedirectOnSuccess();
        this.redirectOnFail = appConfig.getLoginRedirectOnFail();
        if (authenticationAPIClient == null) {
            final Auth0 auth0 = new Auth0(appConfig.getClientId(), appConfig.getClientSecret(), appConfig.getDomain());
            authenticationAPIClient = new AuthenticationAPIClient(auth0);
        }
    }

    /**
     * Entry point
     */
    public void handle(final HttpServletRequest req, final HttpServletResponse res)
            throws IOException, ServletException {
        if (isValidRequest(req)) {
            try {
                final Tokens tokens = fetchTokens(req);
                final UserProfile userProfile = fetchUserProfile(tokens);
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

    protected Tokens fetchTokens(final HttpServletRequest req) throws IOException {
        final String authorizationCode = getAuthorizationCode(req);
        final String redirectUri = req.getRequestURL().toString();
        try {
            final Credentials creds = authenticationAPIClient
                    .token(authorizationCode, redirectUri)
                    .setClientSecret(appConfig.getClientSecret()).execute();
            final Tokens tokens = new Tokens(creds.getIdToken(), creds.getAccessToken(), creds.getType(), creds.getRefreshToken());
            return tokens;
        } catch (Auth0Exception e) {
            throw new IllegalStateException("Cannot get Token from Auth0", e);
        }
    }

    protected UserProfile fetchUserProfile(final Tokens tokens) {
        final String idToken = tokens.getIdToken();
        try {
            final UserProfile profile = authenticationAPIClient.tokenInfo(idToken).execute();
            return profile;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot get Auth0User from Auth0", ex);
        }
    }

    protected String getAuthorizationCode(final HttpServletRequest req) {
        final String code = req.getParameter("code");
        Validate.notNull(code);
        return code;
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
