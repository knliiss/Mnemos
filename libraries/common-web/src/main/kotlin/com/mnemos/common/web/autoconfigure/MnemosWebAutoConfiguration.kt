package com.mnemos.common.web.autoconfigure

import com.mnemos.common.web.logging.RequestIdFilter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class MnemosWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RequestIdFilter::class)
    fun requestIdFilter() = RequestIdFilter()

}