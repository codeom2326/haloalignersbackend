package com.haloalligners.security

import com.haloalligners.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return User.builder()
            .username(userEntity.username)
            .password(userEntity.password)
            .roles(userEntity.role)
            .build()
    }
}