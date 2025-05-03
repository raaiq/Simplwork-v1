package com.example.demo.Domain.RolesAndAuthorities;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BranchRole {

    OWNER(BranchAuthority.POST_CREATE, BranchAuthority.POST_MODIFY, BranchAuthority.MODIFY_INFO,BranchAuthority.POST_READ,BranchAuthority.POST_DELETE,BranchAuthority.SHIFT_MODIFY,BranchAuthority.SHIFT_CREATE,BranchAuthority.SHIFT_DELETE),
    SUPER_OWNER(BranchAuthority.POST_CREATE, BranchAuthority.POST_MODIFY, BranchAuthority.MODIFY_INFO,BranchAuthority.POST_READ,BranchAuthority.POST_DELETE,BranchAuthority.SHIFT_MODIFY,BranchAuthority.SHIFT_CREATE,BranchAuthority.SHIFT_DELETE);

    private final List<BranchAuthority> associatedAuthorities;

    BranchRole(BranchAuthority... authorities) {
        associatedAuthorities=new ArrayList<>();
        associatedAuthorities.addAll(Arrays.asList(authorities));
    }

    //Remove if possible
    public List<SimpleGrantedAuthority> getAuthorities(String companyName){

        return associatedAuthorities.stream().
                map(e-> new SimpleGrantedAuthority(companyName+":"+e.name())).
                collect(Collectors.toList());
    }

    public List<BranchAuthority> getAuthorities(){
        return associatedAuthorities;
    }

    public boolean hasAuthority(String authority){
        return associatedAuthorities.stream().anyMatch(e->e.name().toUpperCase().equals(authority.toLowerCase()));
    }

    public boolean hasAuthority(BranchAuthority authority){
        return associatedAuthorities.stream().anyMatch(e->e.equals(authority));
    }
}
