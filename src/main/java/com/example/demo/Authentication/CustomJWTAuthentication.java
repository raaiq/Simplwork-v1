package com.example.demo.Authentication;

import com.example.demo.Domain.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public class CustomJWTAuthentication extends JwtAuthenticationToken implements UserInfoInterface{
    private UserInfo userInfo;

    public CustomJWTAuthentication(Jwt jwt, UserInfo userInfo) {
        super(jwt);
        this.userInfo = userInfo;
    }

    public CustomJWTAuthentication(Jwt jwt, Collection<? extends GrantedAuthority> authorities, UserInfo userInfo) {
        super(jwt, authorities);
        this.userInfo = userInfo;
    }

    public UserInfo getUser(){
        return this.userInfo;
    }

    public void updateUser(UserInfo updatedUserInfo){
        if(this.userInfo.isSameUser(updatedUserInfo)){
            this.userInfo = updatedUserInfo;
        }
    }

    @Override
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public void setUserInfo(UserInfo user) {
        this.userInfo=user;
    }
}
