package com.auth0.example;

import com.auth0.web.Auth0CallbackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CallbackController {

    @Autowired
    protected Auth0CallbackHandler callback;

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        callback.handle(req, res);
    }

}
