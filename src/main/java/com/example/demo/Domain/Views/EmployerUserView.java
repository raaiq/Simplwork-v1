package com.example.demo.Domain.Views;

import com.example.demo.Domain.EmployerUser;
import com.example.demo.Domain.RolesAndAuthorities.EmployerRole;

import java.util.HashSet;
import java.util.Set;

public class EmployerUserView {

    public Set<EmployerRole> roles;
    public EmployerView employer;

    public EmployerUserView(EmployerUser empUser){
        roles=empUser.getRoles();
        employer=new EmployerView();


        var employerInfo=empUser.getEmployer();
        employer.setCompanyName(employerInfo.getCompanyName());;
        employer.setImageID( employerInfo.getCompanyImage()!=null ? employerInfo.getCompanyImage().getImageID():null);
    }

}
