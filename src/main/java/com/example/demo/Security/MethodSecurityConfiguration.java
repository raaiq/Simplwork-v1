package com.example.demo.Security;

import com.example.demo.Domain.Employer;
import com.example.demo.Repositories.EmployerRepo;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

//TODO: Remove deprecated security config
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    CustomPermissionEvaluator permissionEvaluator;
    MethodSecurityConfiguration(CustomPermissionEvaluator evaluator){
        permissionEvaluator=evaluator;
    }
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler securityExpressionHandler=new DefaultMethodSecurityExpressionHandler();

        securityExpressionHandler.setPermissionEvaluator(permissionEvaluator);
        return securityExpressionHandler;
    }
}
