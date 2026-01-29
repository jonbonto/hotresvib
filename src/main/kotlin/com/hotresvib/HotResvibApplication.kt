package com.hotresvib

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HotResvibApplication

fun main(args: Array<String>) {
    runApplication<HotResvibApplication>(*args)
}
