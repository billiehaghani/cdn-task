package com.bitmovin.platform.challenge.presentation

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(value = [EntityNotFoundException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleEntityNotFoundException(exception: EntityNotFoundException): ErrorResponseDto {
        return ErrorResponseDto(listOf(ErrorResponseDto.ResponseError(exception.message!!, "resource_not_found")))
    }

    @ExceptionHandler(value = [IllegalArgumentException::class])
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleEntityExistsException(exception: IllegalArgumentException): ErrorResponseDto {
        return ErrorResponseDto(listOf(ErrorResponseDto.ResponseError(exception.message!!, "operation_failed")))
    }

    data class ErrorResponseDto(
        val errors: List<ResponseError>,
    ) {
        data class ResponseError(
            val title: String,
            val code: String,
        )
    }
}
