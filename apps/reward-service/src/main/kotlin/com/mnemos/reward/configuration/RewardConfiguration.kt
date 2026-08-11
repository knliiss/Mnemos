package com.mnemos.reward.configuration

import com.mnemos.reward.application.daily.port.out.RewardRandom
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.util.concurrent.ThreadLocalRandom

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RewardProperties::class)
class RewardConfiguration {
    @Bean
    fun rewardClock(): Clock = Clock.systemUTC()

    @Bean
    fun rewardRandom(): RewardRandom =
        object : RewardRandom {
            override fun nextLong(
                fromInclusive: Long,
                toInclusive: Long,
            ): Long = ThreadLocalRandom.current().nextLong(fromInclusive, toInclusive + 1)

            override fun nextDouble(): Double = ThreadLocalRandom.current().nextDouble()
        }
}
