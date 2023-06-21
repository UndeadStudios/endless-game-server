package com.zenyte.api.`in`

import com.google.gson.JsonSyntaxException
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.zenyte.api.model.*
import com.zenyte.api.out.IPS
import com.zenyte.common.ZENYTE_USER_MEMBER_ID
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.gson
import com.zenyte.sql.query.AuthSecretTokenQuery
import com.zenyte.sql.query.adventurers.SelectAdventurerLogQuery
import com.zenyte.sql.query.awards.GetUserAwards
import com.zenyte.sql.query.user.*
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

/**
 * @author Noele, Corey
 */
@RestController
@RequestMapping("/user")
object User {
    
    private val logger = KotlinLogging.logger {}
    private val googleAuthenticator = GoogleAuthenticator()
    
    @GetMapping("/joinDate/{displayName}")
    fun joinDate(@PathVariable displayName: String): String {
        return (ForumJoinDateQuery(displayName).getResults().first as ForumJoinDateQuery.ForumJoinDateResult).date
    }
    
    @GetMapping("/columns/{displayName}")
    fun getColumnsByDisplayName(@PathVariable displayName: String, @RequestParam columns: Array<String>): Map<String, String> {
        logger.info { "[user=$displayName] Querying database for columns: '${columns.joinToString(", ")}'" }
    
        val (results, exception) = UserByNameColumnsQuery(displayName, columns).getResults()
        
        if (exception != null) {
            exception.printStackTrace()
            return mapOf()
        }
    
        return (results as UserByNameColumnsQuery.UserColumnResults).queryResults
    }
    
    @GetMapping("/columnsbyid/{memberId}")
    fun getColumnsByMemberId(@PathVariable memberId: Int, @RequestParam columns: Array<String>): Map<String, String> {
        logger.info { "[user=$memberId] Querying database for columns: '${columns.joinToString(", ")}'" }
        
        val (results, exception) = UserByIdColumnsQuery(memberId, columns).getResults()
        
        if (exception != null) {
            exception.printStackTrace()
            return mapOf()
        }
        
        return (results as UserByIdColumnsQuery.UserColumnResults).queryResults
    }
    
    @GetMapping("/{memberId}/check2fa")
    fun valid2faCode(@PathVariable memberId: Int, @RequestParam code: Int): Boolean {
        logger.info { "[code=$code] Checking 2fa code request for member: $memberId" }
        val (results, exception) = AuthSecretTokenQuery(memberId).getResults()
        if (exception != null) {
            logger.error("[memberId=$memberId] Exception caught while retrieving user 2fa token; returning false", exception)
            return false
        }
        val token = (results as AuthSecretTokenQuery.AuthTokenResult).token
        return googleAuthenticator.authorize(token, code)
    }
    
    @PostMapping("/log/punish")
    fun submitPunishmentLog(@RequestBody punishment: PunishmentLog) {
        logger.info { "Submitting punishment log: $punishment" }
        SubmitPunishmentLogQuery(punishment).getResults()
    
        logger.info { "Sending punishment forum message to user '${punishment.offender}'" }
    
        IPS.sendMessage("Recent offence on your account", """
            Dear <span style="color:#93B045">${punishment.offender}</span>,

            You have received a punishment on your account.

            Punishment type: <span style="color:#00BDF5"><u>${punishment.actionType}</u></span>
            Expires: <span style="color:#00BDF5"><u>${punishment.expires}</u></span>
            Reason: <span style="color:#00BDF5"><u>${punishment.reason}</u></span>

            If you believe this is a mistake or think you were unfairly punished we recommend you appeal the offence <a href="https://vigilancegaming.net/forum/20-appeals/">here</a>.

            We urge you strive to follow the rules in the future.
            You can remind yourself of the rules here:
            
            <iframe allowfullscreen="" class="ipsEmbed_finishedLoading" data-controller="core.front.core.autosizeiframe" data-embedauthorid="404" data-embedcontent="" data-embedid="embed2636546427" scrolling="no" src="https://vigilancegaming.net/topic/282-official-zenyte-rules/?do=embed" style="overflow: hidden; height: 217px; max-width: 502px;"></iframe>
            - Zenyte Staff""".trimIndent().replace("\n", "<br>"), ZENYTE_USER_MEMBER_ID, punishment.offenderUserId)
    }
    
    @PostMapping("/log/trade")
    fun submitTradeLog(@RequestBody transaction: TradeLog) {
        logger.info { "Submitting trade log for users: '${transaction.user}' / '${transaction.partner}'" }
        SubmitTradeLogQuery(transaction).getResults()
    }
    
    @PostMapping("/info")
    fun submitPlayerInformation(@RequestBody info: PlayerInformation) {
        logPlayerSession(info.userId)
        val (_, exception) = SubmitPlayerInformationQuery(info).getResults()
        if (exception != null) {
            logger.error(exception) { "Failed to submit player information for '${info.username}'; $info" }
        } else {
            RedisCache.redis.sync().del(info.redisKey())
            syncPlayerInformation(info)
        }
    }
    
    @GetMapping("/info/{username}")
    fun getPlayerInformation(@PathVariable username: String): PlayerInformation? {
        val fromRedis = RedisCache.redis.sync().get(PlayerInformation.redisKey(username))
        
        if (fromRedis != null) {
            try {
                return gson.fromJson(fromRedis, PlayerInformation::class.java)
            } catch (_: JsonSyntaxException) {
                logger.error { "Invalid player information JSON found in redis for user '$username'; using database instead" }
            }
        }
        
        val (results, exception) = SelectPlayerInformationQuery(username).getResults()
        
        return if (exception != null) {
            logger.error(exception) { "Failed to get player information for '$username'" }
            null
        } else {
            val info = (results as SelectPlayerInformationQuery.SelectPlayerInformationResult).info
            if (info != null) {
                RedisCache.redis.sync().setex(info.redisKey(), 60 * 60, gson.toJson(info))
            }
            info
        }
    }
    
    @GetMapping("/adv/{username}")
    fun getPlayerAdventurerLog(@PathVariable username: String): List<AdventurerLogEntry> {
        val (results, _) = SelectAdventurerLogQuery(username).getResults()
        
        return (results as SelectAdventurerLogQuery.AdventurerLogQueryResults).logEntries
    }
    
    private fun syncPlayerInformation(info: PlayerInformation) {
        syncDonatorRole(info)
    }
    
    private fun syncDonatorRole(info: PlayerInformation) {
        if (info.donatorRole == null) {
            return
        }
        
        val donatorRole = info.donatorRole!!
    
        val donatorRoleGroupIds = Role.DONATOR_ROLES.map { it.forumGroupId }
        val details = getColumnsByMemberId(info.userId, arrayOf("member_group_id", "mgroup_others")).toMutableMap()
    
        val primaryGroup = details.getValue("member_group_id").toInt()
        val secondaryGroups = details.getValue("mgroup_others").split(",").mapNotNull { it.toIntOrNull() }
    
        if (primaryGroup == Role.REGISTERED_MEMBER.forumGroupId || donatorRoleGroupIds.contains(primaryGroup)) {
            details["member_group_id"] = donatorRole.forumGroupId.toString()
        
            // filter out any existing donator roles
            val oldSecondaryGroups = secondaryGroups
                    .filter { !donatorRoleGroupIds.contains(it) }
            details["mgroup_others"] = oldSecondaryGroups.joinToString(",") { it.toString() }
        } else {
            // filter out any existing donator roles and add the new donator role
            val newSecondaryGroups = secondaryGroups
                    .filter { !donatorRoleGroupIds.contains(it) }
                    .plus(donatorRole.forumGroupId)
            details["mgroup_others"] = newSecondaryGroups.joinToString(",") { it.toString() }
        }
        
        val newPrimaryGroup = details.getValue("member_group_id")
        val newSecondaryGroups = details.getValue("mgroup_others")
        
        val oldPrimaryGroup = primaryGroup.toString()
        val oldSecondaryGroups = secondaryGroups.joinToString(",") { it.toString() }
        
        if (oldPrimaryGroup != newPrimaryGroup || oldSecondaryGroups != newSecondaryGroups) {
            logger.info { "Updating donator member groups for '${info.username}'; primary: '$oldPrimaryGroup'=>'$newPrimaryGroup', secondary: '$oldSecondaryGroups'=>'$newSecondaryGroups'" }
            
            val (_, exception) = UpdateUserMemberGroupsQuery(info.userId,
                    newPrimaryGroup,
                    newSecondaryGroups).getResults()
            if (exception != null) {
                logger.error(exception) {
                    "Failed to update forum member groups for user '${info.username}'; " +
                            "member_group_id='$newPrimaryGroup', mgroup_others='$newSecondaryGroups'; $info"
                }
            }
        }
    
    }
    
    private fun logPlayerSession(playerId: Int) {
        UpdateLastActiveDateQuery(playerId).getResults()
    }
    
    @GetMapping("/awards/{username}")
    fun getAwards(@PathVariable username: String): UserAwards? {
        val (results, e) = GetUserAwards(username).getResults()
        
        if (e != null) {
            logger.error(e) { "Error when getting user awards for '$username'" }
            return null
        }
        
        results as GetUserAwards.GetUserAwardsResults
        
        return results.awards
    }
    
}
