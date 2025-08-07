package com.portal.mortgage.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponse<T> {
    int statusCode;
    String status;
    String message;
    T data;
}
