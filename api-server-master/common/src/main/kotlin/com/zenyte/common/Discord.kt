package com.zenyte.common

import com.zenyte.common.datastore.RedisCache

/**
 * @author Corey
 * @since 03/11/2019
 */
object Discord {
    
    const val VERIFIED_DISCORD_KEY_PREFIX = "verified_discord"
    
    fun getVerifiedMemberId(discordId: Long): Int {
        val keys = RedisCache.redis.sync().keys("$VERIFIED_DISCORD_KEY_PREFIX:*:$discordId")
        if (keys.size != 1) {
            throw RuntimeException("Incorrect number of keys returned for discordId '$discordId'")
        }
        return keys[0].split(":")[1].toInt() // format: "verified_discord:23:174170745469927424"
    }
    
}