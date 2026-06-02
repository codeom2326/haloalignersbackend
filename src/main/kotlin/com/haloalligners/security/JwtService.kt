package com.haloalligners.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    // Generate a secure key for HS256 algorithm
    private val secretKey: SecretKey = Jwts.SIG.HS256.key().build()
    private val jwtExpirationMs = 86400000L // 24 hours

    fun generateToken(userDetails: UserDetails): String {
        return Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String? {
        return extractAllClaims(token).subject
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}