package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.request.AuthRequest;
import com.interswitch.bulktransaction.security.JwtTokenProvider;
import com.interswitch.bulktransaction.service.implementation.ClientAuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ClientAuthServiceTest {

    private JwtTokenProvider jwtTokenProvider;
    private ClientAuthService clientAuthService;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        clientAuthService = new ClientAuthService(jwtTokenProvider);
    }

    @Test
    void shouldAuthenticateValidUser() {
        // given
        AuthRequest authRequest = new AuthRequest("greatness", "password1234");
        when(jwtTokenProvider.generateToken(eq("greatness"), eq(List.of("ROLE_USER"))))
                .thenReturn("mocked-jwt-token");

        // when
        String token = clientAuthService.authenticate(authRequest);
    log.info("successfully generated token fo user => {}", authRequest.username() );

        // then
        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);
        verify(jwtTokenProvider, times(1)).generateToken("greatness", List.of("ROLE_USER"));
    }

    @Test
    void shouldAuthenticateValidAdmin() {
        // given
        AuthRequest authRequest = new AuthRequest("noah", "admin");
        when(jwtTokenProvider.generateToken(eq("noah"), eq(List.of("ROLE_ADMIN"))))
                .thenReturn("admin-jwt-token");

        // when
        String token = clientAuthService.authenticate(authRequest);
        log.info("successfully generated token fo user  => {}", authRequest.username() );

        // then
        assertEquals("admin-jwt-token", token);
        verify(jwtTokenProvider, times(1)).generateToken("noah", List.of("ROLE_ADMIN"));
    }

    @Test
    void shouldThrowExceptionForInvalidUsername() {
        // given
        AuthRequest authRequest = new AuthRequest("unknown", "password1234");

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clientAuthService.authenticate(authRequest));

        assertEquals("Invalid Username and Password", ex.getMessage());
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void shouldThrowExceptionForInvalidPassword() {
        // given
        AuthRequest authRequest = new AuthRequest("user", "wrongpassword");

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clientAuthService.authenticate(authRequest));

        assertEquals("Invalid Username and Password", ex.getMessage());
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }
}
