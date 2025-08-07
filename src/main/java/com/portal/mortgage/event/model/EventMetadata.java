package com.portal.mortgage.event.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMetadata {
    private String traceId;
    private long timestamp;
    private String eventType;
    @Builder.Default
    private String version = "1.0";
}
