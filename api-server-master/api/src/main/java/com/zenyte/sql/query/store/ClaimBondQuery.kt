package com.zenyte.sql.query.store

import com.zenyte.api.model.Bond
import com.zenyte.api.model.ClaimBondRequest
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import mu.KotlinLogging
import java.sql.ResultSet

/**
 * @author Corey
 */
class ClaimBondQuery(val request: ClaimBondRequest) : SQLRunnable() {
    
    private val logger = KotlinLogging.logger {}
    
    private val bondEmail = "bonds@zenyte.com"
    
    private val selectTokenQuery = "SELECT id FROM store_payments WHERE address_pass = ? AND email = ?"
    private val rankQuery = "INSERT INTO store_payments ( username, email, item_name, paid, credit_amount, status, client_ip, cvc_pass, zip_pass, address_pass, live_mode )" +
            "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
    
    private val insertElseUpdateCreditQuery = """
        INSERT INTO user_credits (userid, username, credits, total_credits)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY
            UPDATE credits = credits + ?
    """.trimIndent()
    
    private val validResponse = BondClaimResponse(null, true) to null
    private val failedResponse = BondClaimResponse(null, false) to null
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            
            logger.info { "Redeeming bond: $request" }
            HikariPool.getConnection(auth, "endless_main").use { con ->
                val token = request.token
    
                if (token != null && token > 0L) { // backwards compatibility for server without token generation
        
                    // check if token exists
                    con.prepareStatement(selectTokenQuery).use {
                        it.setString(1, request.token.toString())
                        it.setString(2, bondEmail)
    
                        if (it.executeQuery().next()) {
                            logger.info { "[token=${request.token}] Token already exists, returning valid response" }
                            return validResponse
                        }
                    }
                }
    
                con.prepareStatement(insertElseUpdateCreditQuery).use { stmt ->
                    val bond = try {
                        Bond.getBond(request.bondType)
                    } catch (e: NoSuchElementException) {
                        return failedResponse
                    }
                    
                    stmt.setInt(1, request.userId)
                    stmt.setString(2, request.username)
                    stmt.setInt(3, bond.credits)
                    stmt.setInt(4, bond.credits)
                    stmt.setInt(5, bond.credits)
                    
                    val count = stmt.executeUpdate()
                    
                    if (count > 0) {
                        con.prepareStatement(rankQuery).use { rank ->
                            rank.setString(1, request.username)
                            rank.setString(2, bondEmail)
                            rank.setString(3, "${bond.amount}$ bond")
                            rank.setDouble(4, bond.amount.toDouble())
                            rank.setInt(5, bond.credits)
                            rank.setString(6, "Completed")
                            rank.setString(7, request.ipAddress)
                            rank.setString(8, "bond")
                            rank.setString(9, "bond")
                            rank.setString(10, token?.toString() ?: "bond") // backwards compatibility
                            rank.setInt(11, 1)
                            rank.execute()
    
                            return validResponse
                        }
                    }
                    
                    return failedResponse
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return failedResponse
        }
    }
    
    data class BondClaimResponse(override val results: ResultSet?, val success: Boolean) : SQLResults
    
}