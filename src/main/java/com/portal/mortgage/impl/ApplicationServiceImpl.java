package com.portal.mortgage.impl;

import com.portal.mortgage.datamodel.ApplicationStatus;
import com.portal.mortgage.dto.ApplicationData;
import com.portal.mortgage.dto.request.ApplicationRequest;
import com.portal.mortgage.dto.request.DecisionRequest;
import com.portal.mortgage.entity.Application;
import com.portal.mortgage.entity.Decision;
import com.portal.mortgage.entity.User;
import com.portal.mortgage.event.KafkaEventPublisher;
import com.portal.mortgage.exception.ForbiddenException;
import com.portal.mortgage.exception.NotFoundException;
import com.portal.mortgage.mapper.ApplicationMapper;
import com.portal.mortgage.repository.ApplicationRepository;
import com.portal.mortgage.repository.UserRepository;
import com.portal.mortgage.response.GlobalResponse;
import com.portal.mortgage.response.ResponseBuilder;
import com.portal.mortgage.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ResponseBuilder responseBuilder;

    @Override
    public GlobalResponse<ApplicationData> createApplication(ApplicationRequest request, String username) {

        log.info("Creating application for user: {}", username);

        // Find the applicant or throw an exception if not found.
        User applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        Application application = applicationMapper.toEntity(request);
        application.setApplicant(applicant);
        application.setApplicantName(request.getApplicantName());
        application.setNationalId(request.getNationalId());
        application.setPropertyDetails(request.getPropertyDetails());
        application.setStatus(ApplicationStatus.PENDING);
        application.setDecision(null);

        Application savedApplication = applicationRepository.save(application);

        // Generate a traceId for this transaction. In a real-world scenario, this might
        // come from an incoming request header (e.g., from OpenTelemetry).
        String traceId = UUID.randomUUID().toString();

        // Publish event with the correct arguments: topic, eventType, payload, and traceId.
        kafkaEventPublisher.publishEvent(
                "loan.applications",
                "APPLICATION_CREATED",
                savedApplication,
                traceId
        );

        ApplicationData data = applicationMapper.toDto(savedApplication);
        return responseBuilder.buildResponse(data, HttpStatus.CREATED, "Application created successfully");
    }

    @Override
    public GlobalResponse<ApplicationData> getApplicationById(String id, String username) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // OFFICERS can view any application, APPLICANTS can only view their own.
        if ("APPLICANT".equals(user.getRole().name()) && !application.getApplicant().getUsername().equals(username)) {
            throw new ForbiddenException("You do not have permission to view this application.");
        }

        ApplicationData data = applicationMapper.toDto(application);
        return responseBuilder.buildResponse(data, HttpStatus.OK, "Application retrieved successfully");
    }

    @Override
    public Page<ApplicationData> listApplications(String status, String nationalId, Pageable pageable, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Start with a base specification that can be built upon.
        Specification<Application> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ApplicationStatus.valueOf(status.toUpperCase())));
        }

        if (nationalId != null && !nationalId.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("nationalId"), nationalId));
        }

        // If the user is an APPLICANT, restrict the query to only their applications.
        if ("APPLICANT".equals(user.getRole().name())) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("applicant"), user));
        }

        return applicationRepository.findAll(spec, pageable)
                .map(applicationMapper::toDto);
    }

    @Override
    public GlobalResponse<ApplicationData> updateApplicationStatus(String id, DecisionRequest request, String officerUsername) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        User officer = userRepository.findByUsername(officerUsername)
                .orElseThrow(() -> new NotFoundException("Officer not found with username: " + officerUsername));

        Decision decision = new Decision();
        decision.setApplication(application);
        decision.setOfficer(officer);
        decision.setStatus(request.getStatus());
        decision.setComments(request.getComments());

        application.setStatus(request.getStatus());
        application.setDecision(decision);

        Application updatedApplication = applicationRepository.save(application);

        String traceId = UUID.randomUUID().toString();
        kafkaEventPublisher.publishEvent(
                "loan.applications",
                "APPLICATION_DECISIONED",
                updatedApplication,
                traceId
        );

        ApplicationData data = applicationMapper.toDto(updatedApplication);
        return responseBuilder.buildResponse(data, HttpStatus.OK, "Application status updated successfully");
    }

    @Override
    public GlobalResponse<ApplicationData> deleteApplication(String id, String username) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Only allow the applicant to delete their own application.
        if (!application.getApplicant().getUsername().equals(username)) {
            throw new ForbiddenException("You do not have permission to delete this application.");
        }

        applicationRepository.delete(application);

        String traceId = UUID.randomUUID().toString();

        kafkaEventPublisher.publishEvent(
                "loan.applications",
                "APPLICATION_DELETED",
                application,
                traceId
        );

        return responseBuilder.buildResponse(null, HttpStatus.NO_CONTENT, "Application deleted successfully");
    }
}