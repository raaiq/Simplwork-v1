package com.example.demo.Controllers.Other;

import com.example.demo.Domain.ActionToken;
import com.example.demo.Authentication.ActionTokenAuthentication;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Services.Other.ActionTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Action Token")
@RestController
@Validated
@RequestMapping(path = "api/actionToken")
public class ActionTokenController {


    @Autowired
    private ActionTokenService tokenService;

    @GetMapping
    public void consumeToken(HttpServletRequest request,HttpServletResponse response) throws InternalException {
        ActionToken token=((ActionTokenAuthentication)SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        tokenService.consumeToken(token,request,response);
    }
}
