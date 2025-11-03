package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.dto.request.AuthRequest;
import com.interswitch.bulktransaction.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientAuthService {

    private final JwtTokenProvider jwtUtil;

    private final Map<String,User > users = Map.of(
            "greatness", new User("greatness", "password1234", List.of("ROLE_USER")),
            "noah", new User("noah", "admin", List.of("ROLE_ADMIN"))
    );

    public String authenticate(AuthRequest authRequest) {
        User user = users.get(authRequest.username());
        if (user == null || !user.password.equals(authRequest.password())) {
            throw new IllegalArgumentException("Invalid Username and Password");
        }
        return jwtUtil.generateToken(authRequest.username(), user.roles);
    }

    private record User(String username, String password, List<String> roles) {}
}

