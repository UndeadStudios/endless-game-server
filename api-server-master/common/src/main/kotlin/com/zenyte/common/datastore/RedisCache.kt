package com.zenyte.common.datastore

import com.zenyte.common.gson
import com.zenyte.common.util.getenv
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import mu.KotlinLogging

/**
 * @author Corey
 * @since 14/05/19
 */
object RedisCache {
    
    private val logger = KotlinLogging.logger {}
    
    private val redisClient = RedisClient.create(RedisURI.Builder.redis(getenv("REDIS_HOST", "localhost"), 6379).build())
    
    val redis: StatefulRedisConnection<String, String> by lazy {
        try {
            logger.info { "Trying to connect to Redis..." }
            val redisInit = redisClient.connect()
            redisInit.sync().ping()
            logger.info { "Successfully connected to Redis" }
            redisInit
        } catch (e: RedisConnectionException) {
            logger.error { "Could not connect to Redis!" }
            throw e
        }
    }
    
    /**
     * @param key The key to get the value of.
     * @param clazz The class which corresponds to the value, used for deserialising String into given object type.
     * @return Deserialised object, based on the given clazz.
     */
    fun <T> RedisCommands<String, String>.getObject(key: String, clazz: Class<T>): T? {
        val value = this.get(key) ?: return null
        return gson.fromJson(value, clazz)
    }
    
    /**
     * @param key The key to set the value of.
     * @param `object` The value to serialise and set the value of.
     * @param timeout Optional timeout in seconds until the key expires.
     */
    fun RedisCommands<String, String>.setObject(key: String, `object`: Any, timeout: Int = 0) {
        val value = gson.toJson(`object`)
        if (timeout > 0) {
            this.setex(key, timeout.toLong(), value)
        } else {
            this.set(key, value)
        }
    }
}

