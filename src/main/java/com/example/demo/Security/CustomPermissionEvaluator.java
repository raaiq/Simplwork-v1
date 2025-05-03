package com.example.demo.Security;

import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.Branch;
import com.example.demo.Domain.EmployerUser;
import com.example.demo.Domain.RolesAndAuthorities.BranchAuthority;
import com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.UserInfo;
import com.example.demo.Exceptions.ResourceNotFoundException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

//Have custom method expression handler
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
       return customHasPermission(authentication,targetDomainObject,permission);
    }

    public static boolean customHasPermission(Authentication authentication, Object targetDomainObject, Object permission){
        UserInfo userInfo = ((UserInfoInterface)authentication).getUserInfo();
        //TODO: Take in employer object instead of name string
        if(targetDomainObject instanceof String){
            try {

                EmployerUser employerUser= userInfo.findEmployerUser((String)targetDomainObject);
                return employerUser.getRoles().stream().anyMatch(e->e.hasAuthority((EmployerAuthority) permission));
            } catch (ResourceNotFoundException e) {
                return false;
            }}
        if(targetDomainObject instanceof EmployerBranch){
            try {
                EmployerBranch temp= (EmployerBranch) targetDomainObject;
                if(!userInfo.hasEmployer(temp.getEmployer())) {
                    //TODO:Log permission denied
                    return false;
                }
                Branch branch=temp.getBranch();
                var roleMap= userInfo.findEmployerUser(temp.getEmployer()).getBranchRolesMap();
                if(!roleMap.containsKey(branch)){
                    return false;
                }
                return roleMap.get(branch).stream().anyMatch(e->e.hasAuthority((BranchAuthority) permission));

            } catch (ResourceNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        throw new RuntimeException("This function is not supported, use other overloaded function");

    }

}
