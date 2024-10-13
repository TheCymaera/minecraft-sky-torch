package com.heledron.sky_torch

import com.heledron.sky_torch.laser.*
import com.heledron.sky_torch.utilities.*
import org.bukkit.plugin.java.JavaPlugin

class SkyTorch : JavaPlugin() {
    companion object {
        lateinit var instance: SkyTorch
    }

    override fun reloadConfig() {
        super.reloadConfig()
        val map = configSectionToMap(config.getConfigurationSection("laser")) ?: return
        AppState.options = Serializer.fromMap(map, LaserOptions::class.java)
    }

    fun writeAndSaveConfig() {
        config.set("laser", Serializer.toMap(AppState.options))
        saveConfig()
    }

    override fun onEnable() {
        instance = this

        writeAndSaveConfig()

        onTick {
            for (gameObject in GameObject.live) gameObject.update()
            for (gameObject in GameObject.live) gameObject.render()
            AppState.renderer.flush()
        }

        registerCustomItems()
        registerCommands()
    }

    override fun onDisable() {
        undoSetBlock()
        AppState.close()
    }
}




