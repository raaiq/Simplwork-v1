package com.example.demo.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HTMLEmployerController {
    Logger logger = LoggerFactory.getLogger(HTMLController.class);

    @RequestMapping("/e/{page:^(?!.*[.].*$).*$}")
    public String requestPage(@PathVariable("page") String page) {
        String htmlPage = "/e/"+page+".html";
        logger.info("forwarding request to {}", htmlPage);
        return htmlPage;
    }
}
