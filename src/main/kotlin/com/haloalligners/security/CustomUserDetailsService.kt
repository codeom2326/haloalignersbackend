package com.haloalligners.security

import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository) : UserDetailsService {
    override fun loadUserByUsername(userName: String): UserDetails {
        val userEntity = clinicContactsAndLabPartnersRepository.findByUsername(userName).orElseThrow {UsernameNotFoundException("User not found: $userName")}

        return User.builder()
            .username(userEntity.username)
            .password(userEntity.password)
            .roles(userEntity.role)
            .build()
    }
}