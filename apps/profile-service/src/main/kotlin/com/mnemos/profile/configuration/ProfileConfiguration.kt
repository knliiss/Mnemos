package com.mnemos.profile.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration(proxyBeanMethods = false)
class ProfileConfiguration
