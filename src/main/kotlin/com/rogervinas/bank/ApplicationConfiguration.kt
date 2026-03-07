package com.rogervinas.bank

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.ZoneId

@Configuration
class ApplicationConfiguration {

    @Bean
    fun clock() = java.time.Clock.system(ZoneId.systemDefault())
}