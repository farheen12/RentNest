package com.codingshuttle.projects.airbnbApp.advice;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.util.*;
@Data
@Builder
public class ApiError {
    private HttpStatus status;
    private String message;
    private List<String> subErrors;
}
