package com.zenyte.sql.query.hiscores

import com.zenyte.api.model.ExpMode
import com.zenyte.api.model.HiscoreByColumnEntry
import com.zenyte.api.model.HiscoresByColumn
import com.zenyte.api.model.IronmanMode
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import org.apache.commons.lang3.mutable.MutableInt
import java.sql.ResultSet

/**
 * @author Corey
 */
class HiscoresByColumnQuery(private val column: String,
                            private val page: Int,
                            private val ironmanMode: IronmanMode? = null,
                            private val expMode: ExpMode? = null) : SQLRunnable() {
    
    private val pageSize = 25
    
    private val ironmanMarker = "{ironman}"
    private val expModeMarker = "{expMode}"
    
    private val query = """
                SELECT *
                FROM   (SELECT ( @rank := @rank + 1 ) rank,
                               skill_name,
                               experience,
                               last_modified,
                               username,
                               level,
                               mode,
                               xp_mode
                        FROM skill_hiscores A,
                            (SELECT @rank := 0) B
                        WHERE skill_name = ?
                          $ironmanMarker
                          $expModeMarker
                        ORDER BY level DESC,
                            experience DESC,
                            last_modified ASC) skill_hiscores
                WHERE rank > ? AND rank <= ?
                ORDER BY rank;
                """.trimMargin()
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "endless_main").use { con ->
                val offset = MutableInt(0)
                var queryBuilder = query
                
                queryBuilder = queryBuilder.replaceMarker(offset, ironmanMarker, "AND mode = ?", ironmanMode)
                queryBuilder = queryBuilder.replaceMarker(offset, expModeMarker, "AND xp_mode = ?", expMode)
                
                con.prepareStatement(queryBuilder).use { pst ->
                    pst.setString(1, column.toLowerCase())
                    
                    if (ironmanMode != null) {
                        pst.setInt(2, ironmanMode.id)
                    }
                    if (expMode != null) {
                        pst.setInt(1 + offset.value, expMode.index)
                    }
                    
                    pst.setInt(2 + offset.value, (page - 1) * pageSize)
                    pst.setInt(3 + offset.value, pageSize * page)
                    
                    return Pair(HiscoresByColumnResult(pst.executeQuery(), page, column.toLowerCase()), null)
                }
                
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(HiscoresByColumnResult(null, -1, ""), e)
        }
    }
    
    private fun String.replaceMarker(offset: MutableInt, marker: String, contents: String, source: Any?): String {
        return if (source != null) {
            offset.increment()
            this.replace(marker, contents)
        } else {
            this.replace(marker, "")
        }
    }
    
    data class HiscoresByColumnResult(override val results: ResultSet?, val page: Int, val measurement: String) : SQLResults {
        val result: HiscoresByColumn?
        
        init {
            if (results == null) {
                result = null
            } else {
                val entries = mutableListOf<HiscoreByColumnEntry>()
                
                while (results.next()) {
                    entries.add(HiscoreByColumnEntry(
                            rank = results.getInt("rank"),
                            measurement = results.getString("skill_name"),
                            username = results.getString("username"),
                            level = results.getInt("level"),
                            experience = results.getLong("experience"),
                            mode = IronmanMode.VALUES.first { it.id == results.getInt("mode") },
                            expMode = ExpMode.VALUES.first { it.index == results.getInt("xp_mode") },
                            lastModified = results.getTimestamp("last_modified")
                    ))
                }
                
                result = HiscoresByColumn(measurement, page, entries)
            }
        }
        
    }
    
}