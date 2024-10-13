package com.heledron.sky_torch.utilities

import com.heledron.sky_torch.SkyTorch
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.ChatColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.CommandMinecart
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.*
import java.io.Closeable
import java.lang.Math
import kotlin.math.cos
import kotlin.math.sin


fun playSound(location: Location, sound: org.bukkit.Sound, volume: Float, pitch: Float) {
    location.world!!.playSound(location, sound, volume, pitch)
}

fun playSoundAtPlayers(world: World, sound: org.bukkit.Sound, volume: Float, pitch: Float) {
    for (player in world.players) {
        player.playSound(player.location, sound, volume, pitch)
    }
}

fun <T : Entity> spawnEntity(location: Location, clazz: Class<T>, initializer: (T) -> Unit): T {
    return location.world!!.spawn(location, clazz, initializer)
}

fun spawnParticle(particle: org.bukkit.Particle, location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double) {
    location.world!!.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra)
}

fun <T> spawnParticle(
    particle: org.bukkit.Particle,
    location: Location,
    count: Int,
    offsetX: Double,
    offsetY: Double,
    offsetZ: Double,
    extra: Double,
    data: T
) {
    location.world!!.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data)
}

fun configSectionToMap(section: org.bukkit.configuration.ConfigurationSection?): Map<String, Any>? {
    if (section == null) return null
    val map = mutableMapOf<String, Any>()
    for ((key, value) in section.getValues(false)) {
        if (value is org.bukkit.configuration.ConfigurationSection) {
            map[key] = configSectionToMap(value) as Any
        } else {
            map[key] = value
        }
    }
    return map
}

fun runCommandSilently(command: String) {
    val server = org.bukkit.Bukkit.getServer()
    val location = org.bukkit.Bukkit.getWorlds().first().spawnLocation
    spawnEntity(location, CommandMinecart::class.java) {
        it.setCommand(command)
        server.dispatchCommand(it, command)
        it.remove()
    }
}

fun raycastGround(location: Location, direction: Vector, maxDistance: Double): RayTraceResult? {
    return location.world!!.rayTraceBlocks(location, direction, maxDistance, FluidCollisionMode.NEVER, true)
}

fun sendDebugMessage(message: String) {
    sendActionBar(firstPlayer() ?: return, message)
}

fun firstPlayer(): Player? {
    return SkyTorch.instance.server.onlinePlayers.firstOrNull()
}

fun sendActionBar(player: Player, message: String) {
//    player.sendActionBar(message)
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent(message))
}


fun centredTransform(xSize: Float, ySize: Float, zSize: Float): Transformation {
    return Transformation(
        Vector3f(-xSize / 2, -ySize / 2, -zSize / 2),
        AxisAngle4f(0f, 0f, 0f, 1f),
        Vector3f(xSize, ySize, zSize),
        AxisAngle4f(0f, 0f, 0f, 1f)
    )
}

fun ring(count: Int): List<Pair<Double, Double>> {
    val out = mutableListOf<Pair<Double, Double>>()

    for (i in 0 until count) {
        val angle = i.toDouble() / count * 2 * Math.PI
        out.add(sin(angle) to cos(angle))
    }

    return out
}


fun transformFromMatrix(matrix: Matrix4f): Transformation {
    val translation = matrix.getTranslation(Vector3f())
    val rotation = matrix.getUnnormalizedRotation(Quaternionf())
    val scale = matrix.getScale(Vector3f())

    return Transformation(translation, rotation, scale, Quaternionf())
}

fun applyTransformationWithInterpolation(entity: BlockDisplay, transformation: Transformation) {
    if (entity.transformation != transformation) {
        entity.transformation = transformation
        entity.interpolationDelay = 0
    }
}

fun applyTransformationWithInterpolation(entity: BlockDisplay, matrix: Matrix4f) {
    applyTransformationWithInterpolation(entity, transformFromMatrix(matrix))
}

fun addEventListener(listener: Listener): Closeable {
    SkyTorch.instance.server.pluginManager.registerEvents(listener, SkyTorch.instance)
    return Closeable {
        org.bukkit.event.HandlerList.unregisterAll(listener)
    }
}

fun onGestureUseItem(listener: (Player, ItemStack) -> Unit): Closeable {
    return addEventListener(object : Listener {
        @org.bukkit.event.EventHandler
        fun onPlayerInteract(event: org.bukkit.event.player.PlayerInteractEvent) {
            if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
            if (event.action == Action.RIGHT_CLICK_BLOCK && !(event.clickedBlock?.type?.isInteractable == false || event.player.isSneaking)) return
            listener(event.player, event.item ?: return)
        }
    })
}

fun createNamedItem(material: org.bukkit.Material, name: String): ItemStack {
    val item = ItemStack(material)
    val itemMeta = item.itemMeta ?: throw Exception("ItemMeta is null")
    itemMeta.setItemName(ChatColor.RESET.toString() + name)
    item.itemMeta = itemMeta
    return item
}


fun sphereBlockOffsets(radius: Int) = sequence {
    for (x in -radius..radius) for (z in -radius..radius) for (y in -radius..radius) {
        if (x * x + y * y + z * z > radius * radius) continue
        yield(Vector3i(x, y, z))
    }
}