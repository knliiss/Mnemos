package com.mnemos.bet.configuration.feign

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class FeignHttpMessageConvertersConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun feignHttpMessageConverters(
        customizers: ObjectProvider<ClientHttpMessageConvertersCustomizer>,
        cloudCustomizers: ObjectProvider<HttpMessageConverterCustomizer>,
    ): FeignHttpMessageConverters =
        FeignHttpMessageConverters(customizers, cloudCustomizers).also { converters ->
            converters.converters
        }
}
