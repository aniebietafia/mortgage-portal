package com.portal.mortgage.dto;

import com.portal.mortgage.datamodel.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ApplicationData {
    private String id;
    private String applicantName;
    private BigDecimal loanAmount;
    private String nationalId;
    private String propertyDetails;
    private ApplicationStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
