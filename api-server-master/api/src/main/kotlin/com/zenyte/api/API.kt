package com.zenyte.api

import com.zenyte.common.datastore.RedisCache
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application

object API {
    fun start(args: Array<String>) {
        RedisCache.redis.sync().ping()
        SpringApplication.run(Application::class.java, *args)
    }
}
