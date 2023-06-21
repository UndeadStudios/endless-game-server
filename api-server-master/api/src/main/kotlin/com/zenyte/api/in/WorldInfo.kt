package com.zenyte.api.`in`

import com.zenyte.api.model.World
import com.zenyte.api.model.WorldEvent
import com.zenyte.common.WorldInfo
import com.zenyte.common.WorldInfo.getKey
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.gson
import com.zenyte.sql.query.game.WorldEventsQuery
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


/**
 * @author Corey
 * @since 01/05/18
 */
@RestController
@RequestMapping("/worldinfo")
object WorldInfo {
    
    /**
     * How long world info is cached for
     */
    private const val WORLD_CACHE_TIME = 60 * 5
    
    @GetMapping("/all/slr", produces = ["application/octet-stream"])
    fun binaryList(): ResponseEntity<Resource> {
        val encodedWorlds = encodeWorldList(getAllWorlds().toList())
        
        val buf = Unpooled.buffer().apply {
            writeInt(encodedWorlds.readableBytes())
            writeBytes(encodedWorlds)
        }
        
        val header = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=slr.ws")
            add(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            add(HttpHeaders.CONTENT_LENGTH, buf.readableBytes().toString())
        }
        
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(buf.readableBytes().toLong())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(ByteArrayResource(buf.array()))
    }
    
    private fun encodeWorldList(list: List<World>): ByteBuf {
        fun ByteBuf.writeString(value: String) {
            val bytes = value.toByteArray()
            writeBytes(bytes).writeByte(0)
        }
        
        val buf = Unpooled.buffer()
        
        buf.writeShort(list.size)
        
        list.sortedBy { it.id }.forEach {
            var mask = 0
            
            for (flag in it.flags) {
                if (flag.override) {
                    mask = flag.mask
                    break
                }
                mask = mask or flag.mask
            }
            
            buf.apply {
                writeShort(it.id)
                writeInt(mask)
                writeString(it.address)
                writeString(it.activity)
                writeByte(it.location.id)
                writeShort(it.playerCount)
            }
            
        }
        
        return buf
    }
    
    @PostMapping("/world/update")
    fun updateWorld(@RequestBody world: World) {
        val worldJson = gson.toJson(world)
        
        RedisCache.redis.sync().hmset(world.getKey(), mapOf(
                WorldInfo.Field.JSON.toString() to worldJson,
                WorldInfo.Field.COUNT.toString() to world.playerCount.toString(),
                WorldInfo.Field.UPTIME.toString() to world.uptime.toString(),
                WorldInfo.Field.PLAYERS.toString() to world.playersOnline.toString()
        ))
        RedisCache.redis.sync().expire(world.getKey(), WORLD_CACHE_TIME.toLong())
    }
    
    @GetMapping("/all", produces = ["application/json"])
    fun getAllWorlds(): Array<World> {
        return WorldInfo.getAllWorlds()
    }
    
    @GetMapping("/all/count")
    fun getTotalPlayerCount(): Int {
        return WorldInfo.getTotalPlayerCount()
    }
    
    @GetMapping("/world/{name}", produces = ["application/json"])
    fun getWorld(@PathVariable name: String): String? {
        return WorldInfo.getWorld(name)
    }
    
    @GetMapping("/world/{name}/online")
    fun isOnline(@PathVariable name: String): Boolean {
        return WorldInfo.isOnline(name)
    }
    
    @GetMapping("/world/{name}/count")
    fun getPlayerCountForWorld(@PathVariable name: String): String? {
        return WorldInfo.getPlayerCountForWorld(name)
    }
    
    @GetMapping("/world/{name}/uptime")
    fun getWorldUptime(@PathVariable name: String): String? {
        return WorldInfo.getWorldUptime(name)
    }
    
    @GetMapping("/world/{name}/players", produces = ["application/json"])
    fun getPlayersForWorld(@PathVariable name: String): Array<String> {
        return WorldInfo.getPlayersForWorld(name)
    }

    @GetMapping("/world/{name}/events", produces = ["application/json"])
    fun getEventsForWorld(@PathVariable name: String): ArrayList<WorldEvent>? {
        return (WorldEventsQuery(name).getResults().first as WorldEventsQuery.WorldEventResult).events
    }
    
}
