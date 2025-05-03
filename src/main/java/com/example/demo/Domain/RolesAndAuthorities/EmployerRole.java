package com.example.demo.Domain.RolesAndAuthorities;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//TODO:Have unified system for storing and accessing authorities
public enum EmployerRole {

    OWNER(EmployerAuthority.POST_CREATE, EmployerAuthority.POST_MODIFY, EmployerAuthority.POST_READ,
            EmployerAuthority.MODIFY_INFO,EmployerAuthority.DELETE_EMPLOYER, EmployerAuthority.CREATE_BRANCH,
            EmployerAuthority.DELETE_BRANCH);

    private final List<EmployerAuthority> associatedAuthorities;

    EmployerRole(EmployerAuthority... authorities) {
        associatedAuthorities=new ArrayList<>();
        associatedAuthorities.addAll(Arrays.asList(authorities));
    }

    public List<SimpleGrantedAuthority> getAuthorities(String companyName){

        return associatedAuthorities.stream().
                map(e-> new SimpleGrantedAuthority(companyName+":"+e.name())).
                collect(Collectors.toList());
    }

    public List<EmployerAuthority> getAuthorities(){
        return associatedAuthorities;
    }

    public boolean hasAuthority(String authority){
        return associatedAuthorities.stream().anyMatch(e->e.name().toUpperCase().equals(authority.toLowerCase()));
    }

    public boolean hasAuthority(EmployerAuthority authority){
        return associatedAuthorities.stream().anyMatch(e->e.equals(authority));
    }
}
