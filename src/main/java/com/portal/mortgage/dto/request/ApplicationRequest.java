package com.portal.mortgage.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRequest {
    @NotNull(message = "Applicant name is required")
    private String applicantName;

    @NotNull(message = "National ID is required")
    private String nationalId;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be a positive value")
    private BigDecimal loanAmount;

    @NotNull(message = "Property details are required")
    private String propertyDetails;
}
