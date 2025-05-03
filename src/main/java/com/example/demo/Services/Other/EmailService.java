package com.example.demo.Services.Other;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.UserInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

//TODO:Have HTML parser for variable substitution
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Value("${custom.host.url}")
    private String hostURL;

    private static final Logger logger= LoggerFactory.getLogger(EmailService.class);

    private ResourceLoader resourceLoader;

    public EmailService(){
        resourceLoader= new DefaultResourceLoader();
    }

    public void sendSimpleMessage(SimpleMailMessage message){
        message.setFrom(fromAddress);
        mailSender.send(message);
    }

    public void sendMimeMessage(MimeMessage message) throws MessagingException {
        message.setFrom(fromAddress);
        mailSender.send(message);
    }

    //TODO:Make this method asynchronous
    public void sendJobInterviewCandidateEmail(Match match, UserInfo user, String employerName){

        Map<String,String> variables= new LinkedHashMap<>();
        String positionTitle=match.getPosting().getPositionTitle();

        variables.put("SIMPLWORK_IMAGE_SRC","cid:simplworklogo");
        variables.put("SIMPLWORK_URL",hostURL);
        variables.put("USER",user.getName());
        variables.put("Employer",employerName);
        variables.put("Job Title",positionTitle);

        String htmlString=asString(resourceLoader.getResource("classpath:email-templates/JobInterviewCandidateEmail.html"));

        for (Map.Entry<String,String> pair:variables.entrySet()
             ) {
            htmlString=htmlString.replace("{{"+pair.getKey()+"}}",pair.getValue());
        }

        Resource logo= resourceLoader.getResource("classpath:images/Logo-long.png");

        MimeMessage message= mailSender.createMimeMessage();

        MimeMessageHelper helper;

        try {
            helper=new MimeMessageHelper(message,true);
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setText(htmlString,true);
            helper.addInline("simplworklogo",logo);
            helper.setSubject(employerName + " is requesting to interview for position: "+positionTitle);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        mailSender.send(message);

    }
    public void sendInterviewEmployerEmail(Match match, String candidateName,String employerEmail){

        Branch matchBranch= match.getPosting().getBranch();
        Map<String,String> variables= new LinkedHashMap<>();
        String positionTitle=match.getPosting().getPositionTitle();

        variables.put("SIMPLWORK_IMAGE_SRC","cid:simplworklogo");
        variables.put("SIMPLWORK_URL",hostURL);
        variables.put("Candidate",candidateName);
        variables.put("Employer",matchBranch.getCompany().getCompanyName());
        variables.put("BranchName",matchBranch.getBranchName());
        variables.put("Job Title",positionTitle);

        String htmlString=asString(resourceLoader.getResource("classpath:email-templates/JobInterviewEmployerEmail.html"));

        for (Map.Entry<String,String> pair:variables.entrySet()
        ) {
            htmlString=htmlString.replace("{{"+pair.getKey()+"}}",pair.getValue());
        }

        Resource logo= resourceLoader.getResource("classpath:images/Logo-long.png");

        MimeMessage message= mailSender.createMimeMessage();

        MimeMessageHelper helper;

        try {
            helper=new MimeMessageHelper(message,true);
            helper.setFrom(fromAddress);
            helper.setTo(employerEmail);
            helper.setText(htmlString,true);
            helper.addInline("simplworklogo",logo);
            helper.setSubject(candidateName+" is ready to interview for position: "+positionTitle);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        mailSender.send(message);

    }

    private static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
