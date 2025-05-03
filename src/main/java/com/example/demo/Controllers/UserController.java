package com.example.demo.Controllers;

import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Domain.Views.EmployerUserView;
import com.example.demo.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(path = "api/user",produces = "application/json")
public class UserController {

    @Autowired
    private UserRepo userRepo;


    @GetMapping(path = "/employerList")
    @ResponseStatus(HttpStatus.OK)
    List<EmployerUserView> getEmployerList(){
        var user=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        return user.getEmployerUserSet().stream()
                .map(ViewDirector::getEmployerUserView)
                .collect(Collectors.toList());
    }
}
