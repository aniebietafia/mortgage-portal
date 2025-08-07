package com.portal.mortgage.mapper;

import com.portal.mortgage.dto.ApplicationData;
import com.portal.mortgage.dto.request.ApplicationRequest;
import com.portal.mortgage.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    Application toEntity(ApplicationRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "applicantName", target = "applicantName")
    @Mapping(source = "loanAmount", target = "loanAmount")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    ApplicationData toDto(Application application);
}