package com.example.demo.Filters;

import com.example.demo.Domain.ActionToken;
import com.example.demo.Authentication.ActionTokenAuthentication;
import com.example.demo.Repositories.ActionTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ActionTokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private ActionTokenRepo repo;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ActionTokenAuthentication auth=(ActionTokenAuthentication) authentication;

        Optional<ActionToken> entry=repo.findById(auth.getPrincipal().getToken());
        if(entry.isEmpty()){
            //TODO: Change message
            throw new BadCredentialsException("Invalid token");
        }
        auth= ActionTokenAuthentication.authenticated(entry.get(), List.of(new SimpleGrantedAuthority("ROLE_USER")));

        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == ActionTokenAuthentication.class;
    }
}
