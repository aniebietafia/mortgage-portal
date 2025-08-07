package com.portal.mortgage.dto.request;

import com.portal.mortgage.datamodel.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecisionRequest {
    @NotNull(message = "Application ID is required")
    private ApplicationStatus status;

    @NotNull(message = "Comments are required")
    private String comments;
}
