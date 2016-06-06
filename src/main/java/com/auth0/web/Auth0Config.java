package com.auth0.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 * Holds the default configuration for the library
 * Taken from properties files
 *
 * Also initialises the Filter Servlet (Auth0Filter) for
 * secured URL endpoint interception
 *
 */
@Component
@Configuration
@ConfigurationProperties("auth0")
@PropertySources({@PropertySource("classpath:auth0.properties")})
public class Auth0Config {

    @ConditionalOnProperty(prefix = "auth0", name = "servletFilterEnabled")
    @Bean
    public FilterRegistrationBean filterRegistration() {
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new Auth0Filter(this));
        registration.addUrlPatterns(securedRoute);
        registration.addInitParameter("redirectOnAuthError", loginRedirectOnFail);
        registration.setName("Auth0Filter");
        return registration;
    }

    private String clientId;
    private String clientSecret;
    private String domain;
    private String onLogoutRedirectTo;
    private String loginRedirectOnSuccess;
    private String loginRedirectOnFail;
    private Boolean servletFilterEnabled;

    private String securedRoute;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOnLogoutRedirectTo() {
        return onLogoutRedirectTo;
    }

    public void setOnLogoutRedirectTo(String onLogoutRedirectTo) {
        this.onLogoutRedirectTo = onLogoutRedirectTo;
    }

    public String getLoginRedirectOnSuccess() {
        return loginRedirectOnSuccess;
    }

    public void setLoginRedirectOnSuccess(String loginRedirectOnSuccess) {
        this.loginRedirectOnSuccess = loginRedirectOnSuccess;
    }

    public String getLoginRedirectOnFail() {
        return loginRedirectOnFail;
    }

    public void setLoginRedirectOnFail(String loginRedirectOnFail) {
        this.loginRedirectOnFail = loginRedirectOnFail;
    }

    public String getSecuredRoute() {
        return securedRoute;
    }

    public void setSecuredRoute(String securedRoute) {
        this.securedRoute = securedRoute;
    }

    public Boolean getServletFilterEnabled() {
        return servletFilterEnabled;
    }

    public void setServletFilterEnabled(Boolean servletFilterEnabled) {
        this.servletFilterEnabled = servletFilterEnabled;
    }
}
