package com.auth0.web;

import com.auth0.authentication.result.UserIdentity;
import com.auth0.authentication.result.UserProfile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience wrapper around the Auth0 UserProfile
 * object (GSON) and implements Principal interface
 */
public class Auth0User implements Principal {

    // subclass to access..
    protected final UserProfile userProfile;

    public Auth0User(final UserProfile userProfile) {
       this.userProfile = userProfile;
    }

    public String getName() {
        return userProfile.getName();
    }

    public String getEmail() {
        return userProfile.getEmail();
    }

    public boolean getEmailVerified() {
//        return userProfile.getEmailVerified();
        //@TODO - temporary hack until available
        return true;
    }

    public String getUserId() {
        return userProfile.getId();
    }

    public String getNickname() {
        return userProfile.getNickname();
    }

    public String getPicture() {
        return userProfile.getPictureURL();
    }

    public List<UserIdentity> getIdentities() {
        return userProfile.getIdentities();
    }

    // @TODO - temporary hack until Roles / Groups support added to UserProfile object
    public List<String> getRoles() {
//        return userProfile.getRoles()
        final List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        return roles;
    }

    // @TODO - temporary hack until Roles / Groups support added to UserProfile object
    public List<String> getGroups() {
//        return userProfile.getGroups();
        final List<String> groups = new ArrayList<>();
        groups.add("GROUP_USER");
        groups.add("GROUP_ADMIN");
        return groups;
    }

    //@TODO - implement equals, hashcode and toString correctly for this Principal

}
