package com.example.demo.Domain.RolesAndAuthorities;

//TODO: Maybe merge shift and post authorities;
public enum BranchAuthority {
    MODIFY_INFO,
    POST_READ, //Also reads the candidates associated with the post
    POST_MODIFY,
    POST_CREATE,
    POST_DELETE,

    SHIFT_CREATE,

    SHIFT_MODIFY,
    SHIFT_DELETE;
}
