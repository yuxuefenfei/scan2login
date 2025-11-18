package com.wwz.login.qrauth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateToken(String userId, String username) {

        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndGetUserId(String token) {
        try {
            var jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String validateTokenAndGetUsername(String token) {
        try {
            var jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return jwt.getClaim("username").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
