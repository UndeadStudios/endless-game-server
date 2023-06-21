package com.zenyte.sql.query.adventurers

import com.zenyte.api.model.AdventurerLogEntry
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import okhttp3.internal.toImmutableList
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @author Corey
 * @since 18/02/2020
 */
class SelectAdventurerLogQuery(private val username: String) : SQLRunnable() {
    
    private val selectQuery = """
        SELECT icon, message, date
        FROM advlog_game
        WHERE user = ?
    """.trimIndent()
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "endless_main").use { con ->
                con.prepareStatement(selectQuery).use {
                    it.setString(1, username.replace(" ", "_"))
                    
                    return AdventurerLogQueryResults(it.executeQuery(), username) to null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return NoneResult() to e
        }
    }
    
    data class AdventurerLogQueryResults(override val results: ResultSet?, private val username: String) : SQLResults {
        val logEntries: List<AdventurerLogEntry>
        
        init {
            if (results != null) {
                val entries = ArrayList<AdventurerLogEntry>()
                
                while (results.next()) {
                    entries.add(AdventurerLogEntry(
                            username,
                            results.getString("icon"),
                            results.getString("message").replace("(<(img|col|shad)=.*>)".toRegex(), ""),
                            date = Date(TimeUnit.SECONDS.toMillis(results.getLong("date")))
                    ))
                }
                
                logEntries = entries.toImmutableList()
            } else {
                logEntries = emptyList()
            }
        }
        
    }
    
}