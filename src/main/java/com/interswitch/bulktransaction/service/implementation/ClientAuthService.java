package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.dto.request.AuthRequest;
import com.interswitch.bulktransaction.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
/**
 *  Responsible for handling client authentication within the bulk transaction system.
 * <p>
 * This service validates client credentials against a predefined in-memory user store,
 * and upon successful authentication, generates a JSON Web Token (JWT) for the client.
 * The JWT token is later used to authorize requests to protected endpoints.
 * </p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *     <li>Validate incoming authentication requests using username and password.</li>
 *     <li>Issue JWT tokens for successfully authenticated clients.</li>
 *     <li>Support role-based authorization by embedding roles within the JWT.</li>
 * </ul>
 *
 *
 * <p>This class uses {@link JwtTokenProvider} to generate secure JWT tokens.</p>
 */

@Service
@RequiredArgsConstructor
public class ClientAuthService {
    /**
     * Utility class responsible for generating JWT tokens.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * In-memory user store containing predefined client credentials and roles.
     * <p>
     * For demonstration purposes, this service uses a static map instead of a database.
     * In a production-grade application, this would typically be replaced by a persistent user store.
     * </p>
     */
    private final Map<String,User > users = Map.of(
            "greatness", new User("greatness", "password1234", List.of("ROLE_USER")),
            "noah", new User("noah", "admin", List.of("ROLE_ADMIN"))
    );

    /**
     * Authenticates a client using the provided {@link AuthRequest}.
     * <p>
     * If the username and password match a record in the in-memory user store,
     * a JWT token is generated and returned. Otherwise, an exception is thrown.
     * </p>
     *
     * @param authRequest The authentication request containing the client's username and password.
     * @return A signed JWT token representing the authenticated user.
     * @throws IllegalArgumentException if the username is not found or the password is incorrect.
     */
    public String authenticate(AuthRequest authRequest) {
        User user = users.get(authRequest.username());
        if (user == null || !user.password.equals(authRequest.password())) {
            throw new IllegalArgumentException("Invalid Username and Password");
        }
        return jwtTokenProvider.generateToken(authRequest.username(), user.roles);
    }

    /**
     * Internal record representing a client user with credentials and assigned roles.
     * <p>
     * This is a private static representation of a user entity containing:
     * <ul>
     *     <li>Username</li>
     *     <li>Password</li>
     *     <li>Roles (for authorization)</li>
     * </ul>
     * </p>
     *
     * @param username The client's username.
     * @param password The client's password.
     * @param roles    The list of roles assigned to the user.
     */
    private record User(String username, String password, List<String> roles) {}
}

