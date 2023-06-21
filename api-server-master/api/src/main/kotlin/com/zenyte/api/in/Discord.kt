package com.zenyte.api.`in`

import com.zenyte.api.model.Role
import com.zenyte.api.out.Discord
import com.zenyte.common.Discord.VERIFIED_DISCORD_KEY_PREFIX
import com.zenyte.common.datastore.RedisCache
import com.zenyte.sql.query.user.InsertDiscordVerification
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/discord")
object Discord {
    
    private val logger = KotlinLogging.logger {}
    
    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Error querying Discord")
    class DiscordQueryException : RuntimeException()
    
    @PostMapping("/user/{userId}/grantrole/{roleId}")
    fun grantUserRole(@PathVariable userId: Long, @PathVariable roleId: Long) {
        try {
            Discord.assignRoles(userId, roleId)
        } catch (e: RuntimeException) {
            logger.error(e) {
                "Failed to assign role $roleId to user $userId"
            }
            throw DiscordQueryException()
        }
    }
    
    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid verification code")
    class InvalidVerificationCodeException : RuntimeException()
    
    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "User already verified")
    class AlreadyVerifiedException : RuntimeException()
    
    @PostMapping("/verify/{memberId}")
    fun verifyDiscordAccount(@PathVariable memberId: Int, @RequestParam verificationCode: String) {
        val discordId = RedisCache.redis.sync().get("discord_verification_code:$verificationCode")?.toLong()
                ?: throw InvalidVerificationCodeException()
    
        if (isVerified(memberId)) {
            throw AlreadyVerifiedException()
        }
    
        if (syncRoles(memberId, discordId)) {
            val (_, _) = InsertDiscordVerification(discordId, memberId).getResults()
            RedisCache.redis.sync().set("$VERIFIED_DISCORD_KEY_PREFIX:$memberId:$discordId", "true")
            RedisCache.redis.sync().del("discord_verification_code:$verificationCode")
        }
        
    }
    
    private fun isVerified(memberId: Int): Boolean {
        // if the discord user id is in verified discord user cache
        if (RedisCache.redis.sync().keys("$VERIFIED_DISCORD_KEY_PREFIX:*:${memberId}").size > 0) {
            return true
        }
    
        // if forum member id is in verified discord user cache
        if (RedisCache.redis.sync().keys("$VERIFIED_DISCORD_KEY_PREFIX:$memberId:*").size > 0) {
            return true
        }
    
        // if discord member has verified role
        return Discord.memberHasRole(memberId.toLong(), Role.VERIFIED)
    }
    
    @PostMapping("/sync")
    fun syncRoles(@RequestParam memberId: Int, @RequestParam discordId: Long): Boolean {
        logger.info { "Syncing roles for user '$memberId 'with Discord id '$discordId'" }
        
        val results = User.getColumnsByMemberId(memberId, arrayOf("member_group_id", "mgroup_others"))
        val roleIds = mutableSetOf(Role.VERIFIED.discordRoleId)
        
        results.getValue("mgroup_others")
                .split(",")
                .plus(results.getValue("member_group_id"))
                .filter { !it.isBlank() }
                .map { it.toInt() }
                .map { it.getDiscordRoleFromForumGroup() }
                .forEach {
                    if (it != null) {
                        roleIds.add(it)
                    }
                }
        
        // remove all existing donator roles
        val donatorRoles = Role.DONATOR_ROLES.map { it.discordRoleId }.minus(roleIds)
        Discord.removeRoles(discordId, *donatorRoles.toLongArray())
        
        try {
            Discord.assignRoles(discordId, *roleIds.toLongArray())
        } catch (e: RuntimeException) {
            logger.error(e) {
                "Failed to assign roles $roleIds to user $discordId"
            }
        }
        
        return true
    }
    
    private fun Int.getDiscordRoleFromForumGroup() = Role.FORUM_GROUPS[this]?.discordRoleId
    
}
