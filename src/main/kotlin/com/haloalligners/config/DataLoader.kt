package com.haloalligners.config

import com.haloalligners.model.UserEntity
import com.haloalligners.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepository.findByUsername("superadmin") == null) {
            val superadmin = UserEntity(
                username = "superadmin",
                password = passwordEncoder.encode("admin123"),
                role = "SUPER_ADMIN",
                fullName = "Super Admin",
                email = "superadmin@haloaligners.com",
                phone = "0000000000",
                gstNumber = null,
                clinicName = "HaloAligners HQ"
            )
            userRepository.save(superadmin)
        }
    }
}