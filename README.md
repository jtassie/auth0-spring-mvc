# Auth0 and Java

[Auth0](https://www.auth0.com) is a cloud service that provides a turn-key solution for authentication, authorization and Single Sign On.

You can use  [Auth0](https://www.auth0.com) to add username/password authentication, support for enterprise identity like Active Directory or SAML and also for social identities like Google, Facebook or Salesforce among others to your web, API and mobile native apps.

## Learn how to use it

[Please read this tutorial](https://docs.auth0.com/server-platforms/java-spring-mvc) to learn how to use this SDK.

You may also find our Sample projects the easiest way to learn simply by installing and running, then inspecting the samples code.


## Default Configuration

### Auth0Config

This holds the default Spring configuration for the library.

```
@Configuration
@ConfigurationProperties("auth0")
@PropertySources({@PropertySource("classpath:auth0.properties")})
```

Note the above. The expectation is that this library will find `auth0.properties` on the classpath.
If you are writing your Client application using `Spring Boot` for example, this is as simple as dropping
the following file into the `src/main/resources` directory alongside `application.properties`.

Here is an example:

```
auth0.domain: {your domain}
auth0.clientId: {your client id}
auth0.clientSecret: {your secret}
auth0.onLogoutRedirectTo: /login
auth0.securedRoute: /portal/*
auth0.loginCallback: /callback
auth0.loginRedirectOnSuccess: /portal/home
auth0.loginRedirectOnFail: /login
auth0.servletFilterEnabled: true
```

Please take a look at the sample that accompanies this library for an easy seed project to see this working.

Here is a breakdown of what each attribute means:

`auth0.domain` - This is your auth0 domain (tenant you have created when registering with auth0 - account name)

`auth0.clientId` - This is the client id of your auth0 application (see Settings page on auth0 dashboard)

`auth0.clientSecret` - This is the client secret of your auth0 application (see Settings page on auth0 dashboard)

`auth0.onLogoutRedirectTo` - This is the page / view that users of your site are redirected to on logout. Should start with `/`

`auth0.securedRoute`: - This is the URL pattern to secure a URL endpoint. Should start with `/`

`auth0.loginCallback` -  This is the URL context path for the login callback endpoint. Should start with `/`

`auth0.loginRedirectOnSuccess` - This is the landing page URL context path for a successful authentication. Should start with `/`

`auth0.loginRedirectOnFail` - This is the URL context path for the page to redirect to upon failure. Should start with `/`

`auth0.servletFilterEnabled` - This is a boolean value that switches having an authentication filter enabled On / Off.


## Extension Points in Library

Most of the library can be extended, overridden or altered according to need. Bear in mind also that this library can
be leveraged as a dependency by other libraries for feaatures such as the Auth0CallbackHandler, NonceGenerator and SessionUtils.
But perhaps the library depending on this library has its own Security Filter solution. Because we are using Spring and may wish
to `deactivate` the Auth0Filter then simply set the properties entry for `auth0.servletFilterEnabled` to `false`. This will exclude
injection of the Auth0Filter when parsing the Java Spring context class.

### Auth0CallbackHandler

Designed to be very flexible, you can choose between composition and inheritance to declare a Controller that delegates
to this CallbackHandler - expects to receive an authorization code - using OIDC / Oauth2 Authorization Code Grant Flow.
Using inheritance offers full opportunities to override specific methods and use alternate implementations override specific
methods and use alternate implementations.

##### Example usages

Example usage - Simply extend this class and define Controller in subclass

```
package com.auth0.example;

import com.auth0.web.Auth0CallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

 @Controller
 public class CallbackController extends Auth0CallbackHandler {

     @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
     protected void callback(final HttpServletRequest req, final HttpServletResponse res)
                                                     throws ServletException, IOException {
         super.handle(req, res);
     }
 }
```

Example usage - Create a Controller, and use composition, simply pass your action requests on to the handle(req, res) method of this delegate class.

```
package com.auth0.example;

import Auth0CallbackHandler;
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

   @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
   protected void callback(final HttpServletRequest req, final HttpServletResponse res)
                                                   throws ServletException, IOException {
       callback.handle(req, res);
   }
}

```

List of functions available for override:


#### protected void onSuccess(HttpServletRequest req, HttpServletResponse resp)

Here you can configure what to do after successful authentication. Uses `auth0.loginRedirectOnSuccess` property

####	protected void onFailure(HttpServletRequest req, HttpServletResponse resp, Exception ex)

Here you can configure what to do after failure authentication. Uses `auth0.loginRedirectOnFail` property

####  protected void store(final Credentials tokens, final Auth0User user, final HttpServletRequest req)

Here you can configure where to store the Tokens and the User. By default, they're stored in the `Session` in the `tokens` and `auth0User` fields

#### protected boolean isValidState(final HttpServletRequest req)

By default, this library expects a Nonce value in the state query param as follows `state=nonce=xyz` where `xyz` is a randomly generated UUID.
The NonceFactory can be used to generate such a nonce value. State may be needed to hold other attribute values hence
why it has its own keyed value of `nonce=xyz`. For instance in SSO you may need an `externalCallbackUrl` which also needs
to be stored down in the state param - `state=nonce=xyz&externalCallbackUrl=abc`

#### protected static Map<String, String> splitQuery(final String query) throws UnsupportedEncodingException

Used to parse the callback query parameters and return a Map of the key value pairs. For instance, would parse
`state=nonce=xyz&externalCallbackUrl=abc` into Map containing keys `nonce` and `externalCallbackUrl` with associated values


### Auth0 Filter

Customise according to need. Default behaviour is to test for presence of `Auth0User` and `Tokens` acquired after authentication
callback. And to parse and verify the validity (including expiration) of the associated JWT id_token.

Location of failed authorizations is configured using `auth0.loginRedirectOnFail` property

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free account in Auth0

1. Go to [Auth0](https://auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## Author

[Auth0](auth0.com)

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE.txt) file for more info.
