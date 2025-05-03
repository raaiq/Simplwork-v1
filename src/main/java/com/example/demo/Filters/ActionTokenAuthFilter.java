package com.example.demo.Filters;

import com.example.demo.Authentication.ActionTokenAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//TODO: Implement this as authentication manager etc

public class ActionTokenAuthFilter extends OncePerRequestFilter {

    private ActionTokenAuthenticationProvider provider;

    public ActionTokenAuthFilter(ActionTokenAuthenticationProvider provider){
        this.provider=provider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token=request.getParameter("token");

        if(token==null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate","Token Error: \"Token not found in request parameters\"");
            return;
        }
        Authentication authentication;
        try {
           authentication=provider.authenticate(new ActionTokenAuthentication(token));
        }catch (BadCredentialsException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate","Token Error: \""+e.getMessage()+"\"");
            return;
        }

        if(authentication ==null){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request,response);

    }
}
