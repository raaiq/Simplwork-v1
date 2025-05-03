package com.example.demo.Authentication;

import com.example.demo.Domain.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

//TODO:Get userinfo through principal of authentication object
public interface UserInfoInterface {
     UserInfo getUserInfo();

     void setUserInfo(UserInfo user);
}
