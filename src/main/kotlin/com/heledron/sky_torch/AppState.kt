package com.heledron.sky_torch

import com.heledron.sky_torch.laser.LaserOptions
import com.heledron.sky_torch.utilities.MultiModelRenderer
import org.bukkit.Location

object AppState {
    val renderer = MultiModelRenderer()
    var options = LaserOptions()
    var target: Location? = null

    fun close() {
        renderer.close()
    }
}


abstract class GameObject {
    companion object {
        private val liveGameObjects = mutableListOf<GameObject>()
        val live: List<GameObject> get() = liveGameObjects.toList()
    }

    init {
        @Suppress("LeakingThis")
        liveGameObjects += this
    }

    open fun remove() {
        liveGameObjects.remove(this)
    }

    abstract fun update()
    abstract fun render()
}
