package com.portal.mortgage.auth.controller;

import com.portal.mortgage.auth.dto.UserData;
import com.portal.mortgage.auth.dto.request.SignInRequest;
import com.portal.mortgage.auth.dto.request.SignUpRequest;
import com.portal.mortgage.auth.service.AuthenticationService;
import com.portal.mortgage.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<UserData>> signup(@RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<GlobalResponse<UserData>> signin(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
