package com.auth0.example;

import com.auth0.web.Auth0Config;
import com.auth0.web.Auth0User;
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
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Auth0Config auth0Config;

    @Autowired
    public HomeController(Auth0Config auth0Config) {
        this.auth0Config = auth0Config;
    }

    @RequestMapping(value="/portal/home", method = RequestMethod.GET)
    protected String home(final Map<String, Object> model, final HttpServletRequest req) {
        logger.info("Home page");
        final Auth0User user = SessionUtils.getAuth0User(req);
        if (user == null) {
            final String logoutPath = auth0Config.getOnLogoutRedirectTo();
            return "redirect:" + logoutPath;
        }
        model.put("user", user);
        return "home";
    }

}
