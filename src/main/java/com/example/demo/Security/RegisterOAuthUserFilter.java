package com.example.demo.Security;

import com.example.demo.Domain.UserInfo;
import com.example.demo.Authentication.CustomJWTAuthentication;
import com.example.demo.Repositories.UserRepo;
import com.example.demo.Services.CandidatePostingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

//TODO: Throw proper http exception if Auth0 is not used
//TODO: Don't need to update job/post service everytime
public class RegisterOAuthUserFilter extends OncePerRequestFilter {


    private  UserRepo userRepo;
    private CandidatePostingService candidatePostingService;
    public RegisterOAuthUserFilter(UserRepo userRepo){
        this.userRepo=userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


       Authentication auth= SecurityContextHolder.getContext().getAuthentication();
       UserInfo userInfo =null;

       if(auth instanceof JwtAuthenticationToken){
           var castedAuth= (JwtAuthenticationToken)auth;

           var claims= castedAuth.getToken().getClaims();
           String email=(String) claims.get("email");
           String sub= (String) claims.get("sub");
           String name= (String) claims.get("name");
           ArrayList<GrantedAuthority> customGrantedAuthorities= new ArrayList<>();
           customGrantedAuthorities.add( new SimpleGrantedAuthority("ROLE_USER"));

           userInfo = userRepo.findByEmail(email).orElseGet(()->{
               UserInfo newUserInfo =new UserInfo(email,name,sub);
               return userRepo.save(newUserInfo);});

           if(!userInfo.getOAuth_ID().equals(sub)){

               //TODO: Properly handle case where different identity provider is used
               filterChain.doFilter(request,response);
           }

           customGrantedAuthorities.addAll(castedAuth.getAuthorities());

           var customAuth=new CustomJWTAuthentication(castedAuth.getToken(), Collections.unmodifiableList(customGrantedAuthorities), userInfo);
           customAuth.setDetails(castedAuth.getDetails());
           customAuth.setAuthenticated(castedAuth.isAuthenticated());
           SecurityContextHolder.getContext().setAuthentication(customAuth);
           filterChain.doFilter(request,response);
       }else {
           filterChain.doFilter(request,response);
       }


    }


}
