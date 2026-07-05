package com.haloalligners.exception

import com.haloalligners.dto.ApiResponse
import org.postgresql.util.PSQLException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.regex.Pattern

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(ex: UsernameNotFoundException, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        val response = ApiResponse<Unit>(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "User not found",
            data = null
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        val response = ApiResponse<Unit>(
            status = HttpStatus.FORBIDDEN.value(),
            message = "You do not have permission to access this resource.",
            data = null
        )
        return ResponseEntity(response, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(DuplicateValueException::class)
    fun handleDuplicateValueException(ex: DuplicateValueException, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        val response = ApiResponse<Unit>(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message.toString(),
            data = null
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        val rootCause = ex.rootCause
        var message = "A database constraint was violated. Please check your input."

        if (rootCause != null && rootCause.message != null) {
            message = parsePsqlExceptionMessage(rootCause.message!!)
        }

        val response = ApiResponse<Unit>(
            status = HttpStatus.BAD_REQUEST.value(),
            message = message,
            data = null
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(PSQLException::class)
    fun handlePsqlException(ex: PSQLException, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        val message = parsePsqlExceptionMessage(ex.message ?: "A database error occurred.")
        val response = ApiResponse<Unit>(
            status = HttpStatus.BAD_REQUEST.value(),
            message = message,
            data = null
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<ApiResponse<Unit>> {
        // Log the exception for debugging purposes
        ex.printStackTrace()

        val response = ApiResponse<Unit>(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred. Please try again later.",
            data = null
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun parsePsqlExceptionMessage(message: String): String {
        val pattern = Pattern.compile("Detail: Key \\((.*?)\\)=\\((.*?)\\) already exists.")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) {
            val key = matcher.group(1)
            val value = matcher.group(2)
            "The value '$value' for the field '$key' already exists. Please use a unique value."
        } else {
            "A database constraint was violated. Please check your input."
        }
    }
}