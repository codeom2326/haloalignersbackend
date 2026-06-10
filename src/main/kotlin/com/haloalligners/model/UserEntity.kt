package com.haloalligners.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String?,

    @Column(nullable = false)
    val role: String = "USER",

    @Column(nullable = false)
    val fullName: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val phone: String,

    @Column
    val gstNumber: String?,

    @Column(nullable = false)
    val clinicName: String
)