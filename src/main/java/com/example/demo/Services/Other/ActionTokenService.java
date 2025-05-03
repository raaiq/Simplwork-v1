package com.example.demo.Services.Other;

import com.example.demo.Domain.ActionToken;
import com.example.demo.Domain.TypesAndEnums.Compositions.JobInterviewDataStruct;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.UserInfo;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Repositories.ActionTokenRepo;
import com.example.demo.Services.CandidatePostingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.module.ResolutionException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActionTokenService {

    @Autowired
    private ActionTokenRepo repo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SecureRandom random;

    @Autowired
    private ApplicationContext context;

    private static final Logger logger= LoggerFactory.getLogger(ActionTokenService.class);

    public String generateActionToken(Method method, Object persistedData, UserInfo user,boolean deleteAfterUse){

        return generateActionToken(method.getName(),
                                    Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList()),
                                    persistedData,user,deleteAfterUse);

    }

    public String generateActionToken(String methodName, List<String> methodTypes, Object persistedData, UserInfo user,boolean deleteAfterUse){
        ActionToken token= new ActionToken();
        byte[] tokenBytes=new byte[210];
        random.nextBytes(tokenBytes);

        token.setToken(removeSpecialCharacters(Base64.getEncoder().encodeToString(tokenBytes)));
        try {
            token.setData(ViewDirector.convertToString(persistedData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        token.setMethodName(methodName);
        token.setMethodArgTypes(methodTypes);
        token.setUser(user);
        token.setDataType(persistedData.getClass().getTypeName());
        token.setDeleteAfterUse(deleteAfterUse);
        token.setCreatedAt(new Date());

        return repo.save(token).getToken();

    }
    //TODO:Have better way to append action token
    public void appendActionToken(String token, String methodName, List<String> argTypes, Object data) throws ResourceNotFoundException {
        ActionToken actionToken= repo.findById(token).orElseThrow(()->new ResolutionException("Couldn't find action token"));
        if(methodName !=null){
            actionToken.setMethodName(methodName);
        }

        if(argTypes !=null){
            actionToken.setMethodArgTypes(argTypes);
        }
        if(data !=null){
            try {
                actionToken.setData(ViewDirector.convertToString(data));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            actionToken.setDataType(data.getClass().getTypeName());
        }
        repo.save(actionToken);
    }

    public void consumeToken(ActionToken token, HttpServletRequest request, HttpServletResponse response) throws InternalException {

        try {
            Class<?> dataType= Class.forName(token.getDataType());
            var storedData= mapper.readValue(token.getData(),dataType);

            List<String> methodArgType= token.getMethodArgTypes();
            Class<?>[] methodTypes=new Class[methodArgType.size()];
            Object[] methodArgs=new Object[methodArgType.size()];
            for (int i = 0; i <methodTypes.length ; i++) {

                methodTypes[i]=Class.forName(methodArgType.get(i));
                //TODO:Implement this better
                if (methodTypes[i].equals(HttpServletRequest.class)) {
                    methodArgs[i]=request;
                }
                else if(methodTypes[i].equals(HttpServletResponse.class)){
                    methodArgs[i]=response;
                }
                else if(methodTypes[i].equals(UserInfo.class)){
                    methodArgs[i]=token.getUser();
                }
                else if(methodTypes[i].equals(dataType)){
                    methodArgs[i]=storedData;
                } else if (methodTypes[i].equals(ActionToken.class)){
                    methodArgs[i]=token;
                }
                else {
                    methodArgs[i]= context.getBean(methodTypes[i]);
                }
            }

            Method method=ActionTokenMethods.class.getMethod(token.getMethodName(),methodTypes);

            method.invoke(null,methodArgs);
            if(token.isDeleteAfterUse()){
                repo.deleteById(token.getToken());
            }

        } catch (Exception e) {
                throw new InternalException("Error occurred in consuming action token:",e);
        }
    }

    private static String removeSpecialCharacters(String string){
        char[] stringArr=string.toCharArray();
        for (int i=0;i<stringArr.length;i++) {
            char c=stringArr[i];
            stringArr[i]=   c=='/'?'a':
                                        c=='+'?'b':
                                                    c=='='?'c':c;
        }
        return String.copyValueOf(stringArr);



    }



    public static class ActionTokenMethods{

        public static void jobInterviewEmailNotificationAccepted(JobInterviewDataStruct data,ActionTokenRepo actionTokenRepo, CandidatePostingService service, HttpServletResponse response)throws Exception{
            jobInterviewEmailNotification(data,actionTokenRepo,service,response,MatchCandidateStatus.ACCEPT_INTERVIEW);
        }

        public static void jobInterviewEmailNotificationRejected(JobInterviewDataStruct data,ActionTokenRepo actionTokenRepo, CandidatePostingService service, HttpServletResponse response)throws Exception{
            jobInterviewEmailNotification(data,actionTokenRepo,service,response,MatchCandidateStatus.WITHDRAWN);
        }

        static  void jobInterviewEmailNotification(JobInterviewDataStruct data, ActionTokenRepo actionTokenRepo, CandidatePostingService service, HttpServletResponse response, MatchCandidateStatus status) throws Exception {

            service.setApplicationStatus(data.getKey().getPostingId(), status);
            actionTokenRepo.deleteById(data.getComplimentaryToken());
            //TODO:Set URL
            response.sendRedirect("https://www.google.ca");
        }
    }
}
