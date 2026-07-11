package com.haloalligners

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class HaloAlignersApplication

fun main(args: Array<String>) {
    println("====== DEBUG ======")
    println("Java timezone: " + java.util.TimeZone.getDefault().id)
    println("user.timezone: " + System.getProperty("user.timezone"))
    println("DATASOURCE_URL: " + System.getenv("DATASOURCE_URL"))
    println("PGOPTIONS: " + System.getenv("PGOPTIONS"))
    println("===================")

    runApplication<HaloAlignersApplication>(*args)
}