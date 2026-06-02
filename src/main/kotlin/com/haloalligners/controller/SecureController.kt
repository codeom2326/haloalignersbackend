package com.haloalligners.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/secure")
class SecureController {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, this is a secure endpoint! You have a valid JWT token."
    }
}