package com.interswitch.bulktransaction.controller;

import com.interswitch.bulktransaction.dto.request.AuthRequest;
import com.interswitch.bulktransaction.dto.response.AuthResponse;
import com.interswitch.bulktransaction.service.implementation.ClientAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final ClientAuthService authService;


    @PostMapping("/authenticate")
    public Mono<AuthResponse> login(@RequestBody AuthRequest request) {
        return Mono.fromSupplier(() -> {
            var token = authService.authenticate(request);
            return new AuthResponse(token);
        });
    }
}
