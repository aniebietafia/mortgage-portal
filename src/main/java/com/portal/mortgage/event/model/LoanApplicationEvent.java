package com.portal.mortgage.event.model;

import com.portal.mortgage.dto.ApplicationData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanApplicationEvent {
    private EventMetadata metadata;
    private ApplicationData payload;
}
