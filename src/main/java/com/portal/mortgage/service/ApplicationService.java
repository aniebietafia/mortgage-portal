package com.portal.mortgage.service;

import com.portal.mortgage.dto.ApplicationData;
import com.portal.mortgage.dto.request.ApplicationRequest;
import com.portal.mortgage.dto.request.DecisionRequest;
import com.portal.mortgage.response.GlobalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    GlobalResponse<ApplicationData> createApplication(ApplicationRequest request, String username);
    GlobalResponse<ApplicationData> getApplicationById(String id, String username);
    Page<ApplicationData> listApplications(String status, String nationalId, Pageable pageable, String username);
    GlobalResponse<ApplicationData> updateApplicationStatus(String id, DecisionRequest request, String officerUsername);
    GlobalResponse<ApplicationData> deleteApplication(String id, String username);
}
