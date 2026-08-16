package com.mnemos.common.web.autoconfigure

import com.mnemos.common.web.exception.GlobalExceptionHandler
import com.mnemos.common.web.exception.ResponseStatusExceptionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class MnemosExceptionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ResponseStatusExceptionHandler::class)
    fun responseStatusExceptionHandler() = ResponseStatusExceptionHandler()

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler::class)
    fun globalExceptionHandler() = GlobalExceptionHandler()
}
