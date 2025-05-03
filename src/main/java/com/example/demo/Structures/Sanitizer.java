package com.example.demo.Structures;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class Sanitizer {

    private static final Logger logger= LoggerFactory.getLogger(Sanitizer.class);
    private static final PolicyFactory sanitizer= Sanitizers.BLOCKS.and(Sanitizers.FORMATTING);

    public static String sanitize(String input,String inputName){
        return sanitizer.sanitize(input,new Sanitizer.Listener(),new Sanitizer.Context(logger,inputName));
    }

    @Data
    @AllArgsConstructor
    private static class Context{
        Logger logger;
        String variableName;
    }

    private static class Listener implements HtmlChangeListener<Sanitizer.Context> {

        @Override
        public void discardedTag(@Nullable Sanitizer.Context context, String s) {
            StackTraceElement[] stack= Thread.currentThread().getStackTrace();
            context.logger.error("Unsafe string detected with forbidden element {} in variable {}. Call stack {}",s,context.variableName,stack);
        }

        @Override
        public void discardedAttributes(@Nullable Sanitizer.Context context, String s, String... strings) {

            StackTraceElement[] stack= Thread.currentThread().getStackTrace();
            context.logger.error("Unsafe string detected with forbidden attribute {} in variable {}. Call stack {}",s,context.variableName,stack);

        }
    }
}
