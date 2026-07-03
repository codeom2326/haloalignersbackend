package com.haloalligners.security

import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val clinicRepository: ClinicContactsAndLabPartnersRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = clinicRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val authorities = listOf(SimpleGrantedAuthority(userEntity.role))

        return User.builder()
            .username(userEntity.username)
            .password(userEntity.password)
            .authorities(authorities)
            .build()
    }
}