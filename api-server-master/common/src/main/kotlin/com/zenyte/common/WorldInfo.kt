package com.zenyte.common

import com.zenyte.api.model.World
import com.zenyte.common.datastore.RedisCache

/**
 * @author Corey
 * @since 02/11/2019
 */
object WorldInfo {
    
    enum class Field {
        COUNT,
        UPTIME,
        JSON,
        PLAYERS
        ;
    }
    
    private const val OBJ_KEY_PREFIX = "world:"
    
    fun World.getKey() = "$OBJ_KEY_PREFIX${this.name}"
    private fun String.getWorldKey() = "$OBJ_KEY_PREFIX${this}"
    
    private fun getWorldForKey(key: String) = RedisCache.redis.sync().hget(key, Field.JSON.toString())
    
    fun getAllWorlds(): Array<World> {
        val worldKeys = RedisCache.redis.sync().keys("$OBJ_KEY_PREFIX*")
        val worldJsons = worldKeys.map { RedisCache.redis.sync().hget(it, Field.JSON.toString()) }
        
        return gson.fromJson(worldJsons.toString(), Array<World>::class.java)
    }
    
    fun getTotalPlayerCount(): Int {
        val worldKeys = RedisCache.redis.sync().keys("$OBJ_KEY_PREFIX*")
        val numbers = worldKeys.map { RedisCache.redis.sync().hget(it, Field.COUNT.toString()) }
        
        return numbers.map { it.toInt() }.sum()
    }
    
    fun getWorld(name: String): String? {
        return getWorldForKey(name.getWorldKey())
    }
    
    fun isOnline(name: String): Boolean {
        return RedisCache.redis.sync().exists(name.getWorldKey()) > 0
    }
    
    fun getPlayerCountForWorld(name: String): String? {
        return RedisCache.redis.sync().hget(name.getWorldKey(), Field.COUNT.toString())
    }
    
    fun getWorldUptime(name: String): String? {
        return RedisCache.redis.sync().hget(name.getWorldKey(), Field.UPTIME.toString())
    }
    
    fun getPlayersForWorld(name: String): Array<String> {
        val players = RedisCache.redis.sync().hget(name.getWorldKey(), Field.PLAYERS.toString())
        
        return gson.fromJson(players, Array<String>::class.java)
    }
    
}