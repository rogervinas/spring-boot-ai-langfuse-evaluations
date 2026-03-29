package com.rogervinas.bank.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@Configuration
class ClockTestConfiguration {

    @Primary
    @Bean
    fun testClock(): Clock = Clock.fixed(Instant.parse("2025-04-15T10:00:00Z"), ZoneOffset.systemDefault())
}
