package com.portal.mortgage.auth.service;

import com.portal.mortgage.auth.dto.UserData;
import com.portal.mortgage.auth.dto.request.SignInRequest;
import com.portal.mortgage.auth.dto.request.SignUpRequest;
import com.portal.mortgage.datamodel.Role;
import com.portal.mortgage.entity.User;
import com.portal.mortgage.jwt.JwtService;
import com.portal.mortgage.repository.UserRepository;
import com.portal.mortgage.response.GlobalResponse;
import com.portal.mortgage.response.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ResponseBuilder responseBuilder;

    @Transactional
    public GlobalResponse<UserData> signup(SignUpRequest request) {
        var user = userRepository.findByUsername(request.getUsername());
        if (user.isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        OffsetDateTime now = OffsetDateTime.now();

        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole() != null ? request.getRole() : Role.APPLICANT);
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        userRepository.save(newUser);
        String token = jwtService.generateToken(newUser);

        UserData data = mapToUserData(newUser, token);

        return responseBuilder.buildResponse(data, HttpStatus.CREATED, "User registered successfully.");
    }

    public GlobalResponse<UserData> signin(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        String token = jwtService.generateToken(user);

        UserData data = mapToUserData(user, token);

        return responseBuilder.buildResponse(data, HttpStatus.OK, "User signed in successfully.");
    }

    private UserData mapToUserData(User user, String token) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setFirstName(user.getFirstName());
        data.setLastName(user.getLastName());
        data.setUsername(user.getUsername());
        data.setRole(user.getRole());
        data.setToken(token);
        data.setCreatedAt(user.getCreatedAt());
        data.setUpdatedAt(user.getUpdatedAt());
        return data;
    }
}
