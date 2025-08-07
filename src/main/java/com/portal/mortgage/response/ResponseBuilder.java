package com.portal.mortgage.response;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseBuilder {
    public <T> GlobalResponse<T> buildResponse(T data, HttpStatus statusCode, String message) {
        GlobalResponse<T> globalResponse = new GlobalResponse<>();
        globalResponse.setStatusCode(statusCode.value());
        globalResponse.setStatus("success");
        globalResponse.setMessage(message);
        globalResponse.setData(data);

        return globalResponse;
    }
}
