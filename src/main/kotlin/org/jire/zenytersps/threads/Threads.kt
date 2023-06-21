package org.jire.zenytersps.threads

import net.openhft.affinity.AffinityLock
import net.openhft.chronicle.core.OS
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Jire
 */
object Threads {

    private val logger: Logger = LoggerFactory.getLogger(Threads::class.java)

    private val availableProcessors =
        1.coerceAtLeast(Runtime.getRuntime().availableProcessors())

    private const val DEFAULT_THREADS_PER_CORE = 1

    val threadsPerCore by lazy {
        if (OS.isWindows()) {
            try {
                var physicalCores = 0

                val exec: Process =
                    Runtime.getRuntime()
                        .exec(
                            arrayOf("wmic", "CPU", "Get", "NumberOfCores", "/Format:List"),
                            emptyArray()
                        )
                exec.inputReader().use { reader ->
                    do {
                        val line = reader.readLine() ?: break
                        if (line.startsWith("NumberOfCores=")) {
                            physicalCores = line.substringAfter("NumberOfCores=").toIntOrNull() ?: continue
                            break
                        }
                    } while (true)
                }

                if (physicalCores > 0) {
                    return@lazy availableProcessors / physicalCores
                }
            } catch (e: Exception) {
                logger.error("Failed to determine threads-per-core on Windows OS", e)
            }
            DEFAULT_THREADS_PER_CORE
        } else AffinityLock.cpuLayout().threadsPerCore()
    }

    fun preciseSleep(
        totalNanos: Long,
        targetBusyWaitingNanos: Long = 100_000_000
    ) {
        val startTime = System.nanoTime()

        // sleeping
        val sleepNanos = totalNanos - targetBusyWaitingNanos - 1_000_000 // extra millisecond because sleep expected
        if (sleepNanos > 0) {
            while (System.nanoTime() - startTime < sleepNanos) {
                Thread.sleep(0)
            }
        }

        // busy-waiting
        while (System.nanoTime() - startTime < totalNanos) {
            Thread.onSpinWait()
        }
    }

}
