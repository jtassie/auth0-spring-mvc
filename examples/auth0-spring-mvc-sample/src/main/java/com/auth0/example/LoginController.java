package com.auth0.example;

import com.auth0.web.Auth0Config;
import com.auth0.web.NonceFactory;
import com.auth0.web.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Auth0Config auth0Config;

    @Autowired
    public LoginController(Auth0Config auth0Config) {
        this.auth0Config = auth0Config;
    }

    @RequestMapping(value="/login", method = RequestMethod.GET)
    protected String login(final Map<String, Object> model, final HttpServletRequest req) {
        logger.debug("Performing login");
        detectError(model);
        initializeNonce(req);
        model.put("clientId", auth0Config.getClientId());
        model.put("domain", auth0Config.getDomain());
        model.put("state", "nonce=" + SessionUtils.getState(req));
        return "login";
    }

    private void initializeNonce(final HttpServletRequest req) {
        if (SessionUtils.getState(req) == null) {
            SessionUtils.setState(req, NonceFactory.create());
        }
    }

    private void detectError(final Map<String, Object> model) {
        if (model.get("error") != null) {
            model.put("error", true);
        } else {
            model.put("error", false);
        }
    }


}
