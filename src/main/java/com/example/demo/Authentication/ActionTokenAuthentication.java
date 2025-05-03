package com.example.demo.Authentication;

import com.example.demo.Domain.ActionToken;
import com.example.demo.Domain.UserInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class ActionTokenAuthentication extends AbstractAuthenticationToken implements UserInfoInterface{

    private final ActionToken principal;

    public ActionTokenAuthentication(ActionToken principal) {
        super(null);
        this.principal = principal;
        this.setAuthenticated(false);
    }

    public ActionTokenAuthentication(String token){
        super(null);
        ActionToken actionToken= new ActionToken();
        actionToken.setToken(token);
        principal=actionToken;
        this.setAuthenticated(false);

    }

    public ActionTokenAuthentication(ActionToken principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    public static ActionTokenAuthentication unauthenticated() {
        return new ActionTokenAuthentication((ActionToken) null);
    }

    public static ActionTokenAuthentication authenticated(ActionToken principal, Collection<? extends GrantedAuthority> authorities) {
        return new ActionTokenAuthentication(principal, authorities);
    }

    public Object getCredentials() {
        return null;
    }

    public ActionToken getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public UserInfo getUserInfo() {
        return principal.getUser();
    }

    @Override
    public void setUserInfo(UserInfo user) {}
}
