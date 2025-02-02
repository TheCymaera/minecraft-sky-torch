package com.heledron.sky_torch

import com.heledron.sky_torch.utilities.sendActionBar
import com.heledron.spideranimation.utilities.CustomItemRegistry
import org.bukkit.Bukkit.createInventory

fun registerCommands() {
    val plugin = SkyTorch.instance

    plugin.getCommand("items")?.setExecutor { sender, _, _, _ ->
        val player = sender as? org.bukkit.entity.Player ?: return@setExecutor true

        val inventory = createInventory(null, 9 * 3, "Items")
        for (item in CustomItemRegistry.items) {
            inventory.addItem(item.defaultItem.clone())
        }

        player.openInventory(inventory)

        return@setExecutor true
    }

    plugin.getCommand("reload_config")?.setExecutor { sender, _, _, _ ->
        plugin.reloadConfig()
        sendMessageOrActionBar(sender, "Config reloaded")

        return@setExecutor true
    }

    plugin.getCommand("preset")?.apply {
        val presets = mapOf(
            "default" to ::presetDefault,

            "orange" to ::presetOrange,
            "blue" to ::presetBlue,
            "purple" to ::presetPurple,

            "big" to ::presetBig,

            "shockwaveVisualizer" to ::presetShockwaveVisualizer,

            "enableFlicker" to ::presetEnableFlicker,
            "disableFlicker" to ::presetDisableFlicker,

            "enableBlinding" to ::presetEnableBlinding,
            "disableBlinding" to ::presetDisableBlinding,
        )

        setExecutor { sender, _, _, args ->
            val preset = args.getOrNull(0) ?: return@setExecutor false
            val presetFunction = presets[preset] ?: return@setExecutor false

            presetFunction()
            sendMessageOrActionBar(sender, "Preset $preset applied")
            SkyTorch.instance.writeAndSaveConfig()

            return@setExecutor true
        }

        setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                return@setTabCompleter presets.keys.toList()
            }
            return@setTabCompleter emptyList()
        }
    }

    plugin.getCommand("scale")?.apply {
        setExecutor { sender, _, _, args ->
            val scale = args.getOrNull(0)?.toDoubleOrNull() ?: return@setExecutor false

            presetSetScale(scale)
            sendMessageOrActionBar(sender, "Scale set to $scale")

            return@setExecutor true
        }
    }
}

private fun sendMessageOrActionBar(sender: org.bukkit.command.CommandSender, message: String) {
    if (sender is org.bukkit.entity.Player) {
        sendActionBar(sender, message)
    } else {
        sender.sendMessage(message)
    }
}