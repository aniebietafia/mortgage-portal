package com.portal.mortgage.controller;

import com.portal.mortgage.dto.ApplicationData;
import com.portal.mortgage.dto.request.ApplicationRequest;
import com.portal.mortgage.dto.request.DecisionRequest;
import com.portal.mortgage.response.GlobalResponse;
import com.portal.mortgage.service.ApplicationService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Validated
@Tag(name = "Mortgage Applications", description = "APIs for creating, viewing, and managing mortgage applications.")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ipAddress) {
        return cache.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String ipAddress) {
        // Allow 20 requests per minute
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Create a new mortgage application", description = "Allows an authenticated applicant to submit a new application.")
    public ResponseEntity<GlobalResponse<ApplicationData>> createApplication(
            @Valid @RequestBody ApplicationRequest request,
            Principal principal,
            HttpServletRequest httpServletRequest) {

        Bucket bucket = resolveBucket(httpServletRequest.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        GlobalResponse<ApplicationData> response = applicationService.createApplication(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('APPLICANT', 'OFFICER')")
    @Operation(summary = "Get an application by its ID", description = "Retrieves a single application. Applicants can only retrieve their own.")
    public ResponseEntity<GlobalResponse<ApplicationData>> getApplicationById(
            @PathVariable String id,
            Principal principal,
            HttpServletRequest httpServletRequest) {

        Bucket bucket = resolveBucket(httpServletRequest.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        GlobalResponse<ApplicationData> response = applicationService.getApplicationById(id, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('APPLICANT', 'OFFICER')")
    @Operation(summary = "List and filter applications", description = "Lists applications with pagination. Officers see all; applicants see only their own.")
    public ResponseEntity<Page<ApplicationData>> listApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String nationalId,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal,
            HttpServletRequest httpServletRequest) {

        Bucket bucket = resolveBucket(httpServletRequest.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        Page<ApplicationData> page = applicationService.listApplications(status, nationalId, pageable, principal.getName());
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('OFFICER')")
    @Operation(summary = "Approve or reject an application", description = "Allows a credit officer to make a decision on a pending application.")
    public ResponseEntity<GlobalResponse<ApplicationData>> decideApplication(
            @PathVariable String id,
            @Valid @RequestBody DecisionRequest request,
            Principal principal,
            HttpServletRequest httpServletRequest) {

        Bucket bucket = resolveBucket(httpServletRequest.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        GlobalResponse<ApplicationData> response = applicationService.updateApplicationStatus(id, request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Delete an application", description = "Allows an applicant to delete their own application.")
    public ResponseEntity<GlobalResponse<ApplicationData>> deleteApplication(
            @PathVariable String id,
            Principal principal,
            HttpServletRequest httpServletRequest) {
        Bucket bucket = resolveBucket(httpServletRequest.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        GlobalResponse<ApplicationData> response = applicationService.deleteApplication(id, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}