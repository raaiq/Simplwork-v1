package com.example.demo.Exceptions.Handler;

import com.example.demo.Exceptions.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//TODO: Better handle json exception e.g field not correct type
//TODO: Allow detailed messages only for development

/**Handles all HTTP exceptions thrown by application code. Converts them to proper HTTP responses*/
@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final String className=GlobalExceptionHandler.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);
    private static final ObjectMapper objectMapper =new ObjectMapper();

    @Autowired
    private Environment environment;

    /**General exception handler for all HTTP exceptions thrown by application through services, controllers, etc
     * The error response includes the status code, it's corresponding string, a short description of the exception caused
     * and URI path causing such exception */
    @ExceptionHandler
    protected ErrorResponse handleHTTPException(HTTPException exception,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,exception.getHTTPStatus());

    }
    @ExceptionHandler
    @ApiResponse(responseCode = "400", description = "General error response. Check response body for more details")
    protected  ErrorResponse handleCustomException(CustomException exception,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ApiResponse(responseCode = "404",description = "Cannot find requested resource")
    protected ErrorResponse handleRelationNotFoundException(ResourceNotFoundException exception,
                                                                     HttpServletRequest request,
                                                                     HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ApiResponse(responseCode = "482",description = "The resource/entity specified in the request body already exists and cannot be persisted")
    protected  ErrorResponse handleCustomRelationAlreadyExistsException(ResourceAlreadyExistsException exception,
                                                                                 HttpServletRequest request,
                                                                                 HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,482,"Resource already exists");
    }
    @ExceptionHandler
    @ApiResponse(responseCode = "403",description = "Inadequate credentials to access resource")
    protected void handleCustomUnauthorizedException(IllegalAccessException exception,
                                                                       HttpServletRequest request,
                                                                       HttpServletResponse response){
         getHTTPErrorResponse(exception,request,response,HttpStatus.FORBIDDEN,"");
    }

    @ExceptionHandler({JsonProcessingException.class, JsonPatchException.class})
    @ApiResponse(responseCode = "481", description = "Semantic or syntactical errors in json object")
    protected ErrorResponse handleJsonException(Exception e,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response){

        String message= "Error processing json object";

        Throwable cause=e.getCause() == null ? e: e.getCause();
        Map<String,String> errors= new HashMap<>();
        StringBuilder fieldError=new StringBuilder();

        //TODO: Implement better
        //TODO: account for more exception cases
        if(e instanceof JsonPatchException){
            message=e.getMessage();
            message = message == null ? "" :message;
        }

        if(JsonMappingException.class.isAssignableFrom(cause.getClass())){
            if(MismatchedInputException.class.isAssignableFrom(cause.getClass())){
                fieldError.append(getTypeFormat(((MismatchedInputException)cause).getTargetType()));
            }
            if(cause instanceof InvalidFormatException exception){
                fieldError.insert(0,exception.getMessage().contains("Cannot deserialize Map key of type") ? "Invalid field name: "+exception.getValue()+". " : "");

            }
            if(fieldError.isEmpty()){
                fieldError.append("Invalid field");
            }
            errors.put(getFieldPath((JsonMappingException) cause),fieldError.toString());

        }
        return getHTTPErrorResponse(e,request,response,481,"Malformed request",message ,errors.isEmpty() ? null:Pair.of("invalidFields",errors));
    }

    @ApiResponse(responseCode = "481",description = "One or more of the fields in the request body, parameters and/or paths are invalid")
    @ExceptionHandler({ConstraintViolationException.class, NonReturnableConstraintViolationException.class, MethodArgumentNotValidException.class})
    protected ErrorResponse handleConstraintViolationException(Exception exception,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response){

        Map<String,String> errors=new HashMap<>();

        //TODO: Have way to differentiate between request param request body and path
        if(exception instanceof MethodArgumentNotValidException){
            ((MethodArgumentNotValidException)exception).getBindingResult().getAllErrors().forEach((e)->errors.put(((FieldError) e).getField(),e.getDefaultMessage()));
        }else {
            ((ConstraintViolationException)exception).getConstraintViolations().forEach((e)->errors.put(e.getPropertyPath().toString(),e.getMessage()));
        }

        if(exception instanceof NonReturnableConstraintViolationException){
            logger.warn("Request wth unsafe payload. Violations :{}",errors);
            logger.warn("Exception:",exception);

            response.setStatus(400);
            return null;
        }

        return getHTTPErrorResponse(exception,request,response,481,"Malformed request","Invalid request parameters, paths and/or body",Pair.of("invalidFields",errors));
    }

    @ExceptionHandler
    @ApiResponse(responseCode = "422",description = "One of the resource specified in the request cannot be found. This resource is not the resource being returned by this method but is related to it")
    protected ErrorResponse handleAuxiliaryResourceNotFoundException(AuxiliaryResourceNotFoundException exception,
                                                                     HttpServletRequest request,
                                                                     HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ErrorResponse handleInternalException(InternalException exception,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response){
        return getHTTPErrorResponse(exception,request,response,HttpStatus.INTERNAL_SERVER_ERROR,"");
    }

    @ExceptionHandler
    @ApiResponse(responseCode = "483",description = "The conditions required to complete the request hae not been met")
    protected  ErrorResponse handleOperationConditionsFailedException(OperationConditionsFailedException exception,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response){
        return  getHTTPErrorResponse(exception,request,response,483,"Operation conditions failed");
    }

    private ErrorResponse getHTTPErrorResponse(Exception exception,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        HttpStatus status,
                                                        Object ...extras){
        return getHTTPErrorResponse(exception,request,response,status.value(),status.name(),extras);
    }

    private ErrorResponse getHTTPErrorResponse(Exception exception,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        int errorCode,
                                                        String errorName,
                                                        Object ...extras){
        //TODO:Might be a security threat with Object value type

        response.setStatus(errorCode);

        ErrorResponse errorBody= new ErrorResponse();
        errorBody.code=errorCode;
        errorBody.errorName=errorName;
        errorBody.uri=request.getRequestURI();

        //TODO: Might not be safe
        while (exception.getCause() != null){
            exception=(Exception) exception.getCause();
        }

        String errorMessage=exception.getMessage();

        for (Object object:extras) {
            if(object ==null){
                continue;
            }
            if(object instanceof String){
                errorMessage=(String) object;
            }
            //TODO:Make cast type safe for map key and value
            if(object.getClass() == Pair.of("",new HashMap<String,String>()).getClass()){
                Pair<String,HashMap<String,String>> pair= (Pair<String, HashMap<String, String>>) object;
                try {
                    errorBody.getClass().getField(pair.getFirst()).set(errorBody,pair.getSecond());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    logger.warn("Error accessing field {} for ErrorResponse entity",pair.getFirst(),e);
                }
            }
        }

        errorBody.message=errorMessage;

        logger.warn("Error occurred in processing request {} with exception ",request,exception);
        return errorBody;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse{
        public int code;
        public String errorName;
        public String message;
        public String uri;

        public Map<String,String> invalidFields;


        @JsonProperty(value = "Invalid fields")
        public Map<String, String> getInvalidFields(){
            return invalidFields;
        }

    }

    private static String getFieldPath(JsonMappingException e){
        List<JsonMappingException.Reference> path=e.getPath();
        StringBuilder sb=new StringBuilder();
        for (JsonMappingException.Reference r:path
             ) {
            sb.append(r.getFieldName()).append(".");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static String getTypeFormat(Class<?> clazz){
        StringBuilder sb=new StringBuilder();
        if(clazz.isEnum()){
            sb.append("Acceptable values are: ");
            Object[] constants=clazz.getEnumConstants();
            for (Object e:constants) {
                sb.append(e).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        } else if(clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)){
            sb.append("Value must be a boolean either: true or false");

        } else if(clazz.isAssignableFrom(double.class) || clazz.isAssignableFrom(Double.class)){
            sb.append("Value must be a floating point");
        }else if(clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class) ||
                 clazz.isAssignableFrom(long.class) || clazz.isAssignableFrom(Long.class) ||
                    clazz.isAssignableFrom(short.class) ||clazz.isAssignableFrom(Short.class)){
            sb.append("Value must be a number");
        } else if(clazz.isAssignableFrom(String.class)){
            sb.append("Value must be string");
        }else if(clazz.isAssignableFrom(LocalDate.class)) {
            sb.append("Value be a date");
        } else {return "";}
        return sb.toString();
    }

}


