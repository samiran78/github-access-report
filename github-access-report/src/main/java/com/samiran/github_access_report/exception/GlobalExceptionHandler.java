package com.samiran.github_access_report.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice //it will check every controller , if any error / exception comes it catches & come here
public class GlobalExceptionHandler {
    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<Map<String,Object>> handleGithubError(GitHubApiException ex){
        log.error("GitHub error: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg=ex.getMessage();
        if(msg.contains("not found")) status=HttpStatus.NOT_FOUND;
        if(msg.contains("invalid") || msg.contains("missing")) status=HttpStatus.UNAUTHORIZED;
        if(msg.contains("permissions")) status=HttpStatus.FORBIDDEN;
        if(msg.contains("rate limit")) status=HttpStatus.TOO_MANY_REQUESTS;
        if(msg.contains("gateway bypass")) status=HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(buildErrorBody(status,msg));
    }
//for handling internal server error
    @ExceptionHandler(Exception.class)
    public  ResponseEntity<Map<String,Object>> handleGeneric(Exception ex){
        log.error("Unexpected error", ex);
        return  ResponseEntity.internalServerError().body(buildErrorBody(HttpStatus.INTERNAL_SERVER_ERROR,"SOMETHING WENT WRONG."));
    }
    private Map<String, Object> buildErrorBody(HttpStatus status, String msg) {
        Map<String, Object> body = new LinkedHashMap<>(); //linkedhashmap for ordered way
        body.put("timestamp", Instant.now().toString());
        body.put("status",status.value());
        body.put("error",status.getReasonPhrase());
        body.put("message",msg);
        return  body;
    }
}
