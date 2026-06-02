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
    val role: String = "USER"
)