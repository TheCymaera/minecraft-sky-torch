package com.heledron.sky_torch.utilities

import com.heledron.sky_torch.SkyTorch
import java.io.Closeable

/**
 * Run a task every tick.
 */
fun onTick(task: () -> Unit): Closeable {
    TaskList.tickTasks.add(task)
    return Closeable { TaskList.tickTasks.remove(task) }
}

/**
 * Run a task at the end of every tick.
 * Typically used for rendering.
 */
fun onTickEnd(task: () -> Unit): Closeable {
    TaskList.tickEndTasks.add(task)
    return Closeable { TaskList.tickEndTasks.remove(task) }
}

/**
 * Run a task after a delay.
 */
fun runLater(delay: Long, task: () -> Unit): Closeable {
    val plugin = SkyTorch.instance
    val handler = plugin.server.scheduler.runTaskLater(plugin, task, delay)
    return Closeable {
        handler.cancel()
    }
}

/**
 * Run a task every period.
 */
fun interval(delay: Long, period: Long, task: () -> Unit): Closeable {
    val plugin = SkyTorch.instance
    val handler = plugin.server.scheduler.runTaskTimer(plugin, task, delay, period)
    return Closeable {
        handler.cancel()
    }
}

/**
 * For scheduling tasks one after another.
 */
class SeriesScheduler {
    var time = 0L

    fun sleep(time: Long) {
        this.time += time
    }

    fun run(task: () -> Unit) {
        runLater(time, task)
    }
}

private object TaskList {
    init {
        interval(0, 1) {
            tickTasks.toList().forEach { it() }
            tickEndTasks.toList().forEach { it() }
        }
    }

    val tickTasks = mutableListOf<() -> Unit>()
    val tickEndTasks = mutableListOf<() -> Unit>()
}
