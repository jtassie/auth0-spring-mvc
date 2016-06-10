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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 * Using composition or inheritance to use this callback handler from a Controller
 *
 *
 * Example usage - Create a Controller, and use composition, simply pass your action requests on to the handle(req, res)
 * method of this delegate class.
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
 *
 */
@Component
public class Auth0CallbackHandler {

    protected  String redirectOnSuccess;
    protected  String redirectOnFail;
    protected Auth0Config appConfig;
    protected  AuthenticationAPIClient authenticationAPIClient;

    @Autowired
    public void setAppConfig(Auth0Config appConfig) {
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
                final Credentials tokens = fetchTokens(req);
                final UserProfile userProfile = fetchUserProfile(tokens);
                store(tokens, new Auth0User(userProfile), req);
                SessionUtils.setState(req, null);
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

    protected void store(final Credentials tokens, final Auth0User user, final HttpServletRequest req) {
        SessionUtils.setTokens(req, tokens);
        SessionUtils.setAuth0User(req, user);
    }

    protected Credentials fetchTokens(final HttpServletRequest req) throws IOException {
        final String authorizationCode = getAuthorizationCode(req);
        final String redirectUri = req.getRequestURL().toString();
        try {
            final Credentials credentials = authenticationAPIClient
                    .token(authorizationCode, redirectUri)
                    .setClientSecret(appConfig.getClientSecret()).execute();
            return credentials;
        } catch (Auth0Exception e) {
            throw new IllegalStateException("Cannot get Token from Auth0", e);
        }
    }

    protected UserProfile fetchUserProfile(final Credentials tokens) {
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
        if (hasError(req) || !isValidState(req)) {
            return false;
        }
        return true;
    }

    protected static boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

    protected boolean isValidState(final HttpServletRequest req) {
        final String stateValue = req.getParameter("state");
        try {
            final String storageState = SessionUtils.getState(req);
            final Map<String, String> pairs = splitQuery(stateValue);
            final String state = pairs.get("nonce");
            return state != null && state.equals(storageState);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    protected static Map<String, String> splitQuery(final String query) throws UnsupportedEncodingException {
        if (query == null) {
            throw new NullPointerException("query cannot be null");
        }
        final Map<String, String> query_pairs = new LinkedHashMap<>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }


}
