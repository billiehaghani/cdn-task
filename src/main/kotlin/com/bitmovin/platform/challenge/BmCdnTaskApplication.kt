package com.bitmovin.platform.challenge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BmCdnTaskApplication

fun main(args: Array<String>) {
    runApplication<BmCdnTaskApplication>(*args)
}
