package com.heledron.sky_torch

import com.heledron.sky_torch.laser.Burner
import com.heledron.sky_torch.laser.LaserPlacement
import com.heledron.sky_torch.laser.spawnLaserWithChargeUp
import com.heledron.sky_torch.laser.spawnSmokeCloud
import com.heledron.sky_torch.utilities.*
import com.heledron.spideranimation.utilities.CustomItem
import com.heledron.spideranimation.utilities.CustomItemRegistry
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random

fun registerCustomItems() {
    fun getTarget(player: Player): Location? {
        val ray = raycastGround(player.eyeLocation, player.eyeLocation.direction, 255.0)
        val location = ray?.hitPosition?.toLocation(player.world)
        if (location == null) {
            sendActionBar(player, "Could not place target")
        }
        return location
    }


//    CustomItemRegistry.items += CustomItem(
//        id = "place target",
//        defaultItem = createNamedItem(Material.ARROW, "Place Target"),
//        onRightClick = { player ->
//            val location = getTarget(player) ?: return@CustomItem
//
//            AppState.target = location
//            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f)
//
//            AppState.onUpdate {
//                AppState.renderer.render("AppState.target", blockModel(
//                    location = location,
//                    init = {
//                        it.block = Material.DIAMOND_BLOCK.createBlockData()
//                        it.transformation = centredTransform(.4f, .4f, .4f)
//                        it.brightness = Display.Brightness(15, 15)
//                    }
//                ))
//            }.let {
//                runLater(5) { it.close() }
//            }
//        }
//    )

    CustomItemRegistry.items += CustomItem(
        id = "fireLaser",
        defaultItem = createNamedItem(Material.AMETHYST_SHARD, "Fire"),
        onRightClick = { player ->
            val hit = getTarget(player)?.toVector() ?: return@CustomItem
            val world = player.world

            val direction = player.location.direction
            direction.y = 0.0
            direction.normalize()

            val origin = hit.clone()
            origin.add(direction.clone().add(Vector(0, 1, 0)).rotateAroundY(Math.PI / 6).multiply(600))

            val renderLocation = player.location.toVector()
            val distance = renderLocation.distance(hit)
            println(distance)

//            if (distance < 64) renderLocation.copy(hit)

            val placement = LaserPlacement(
                world = world,
                origin = origin,
                hit = hit,
                velocityDirection = direction.clone().rotateAroundY(-Math.PI / 12),
                render = renderLocation
            )

            spawnLaserWithChargeUp(placement, AppState.options)
        }
    )


    CustomItemRegistry.items += CustomItem(
        id = "restore",
        defaultItem = createNamedItem(Material.BREEZE_ROD, "Restore"),
        onRightClick = { player ->
            player.playSound(player.location, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 0.0f)

            for (obj in GameObject.live.toList()) obj.remove()

            undoSetBlock()
        }
    )




    CustomItemRegistry.items += CustomItem(
        id = "smoke",
        defaultItem = createNamedItem(Material.FLINT, "Spawn Smoke"),
        onRightClick = { player ->
            val target = getTarget(player) ?: return@CustomItem
            playSound(target, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, Random.nextDouble(1.0,1.5).toFloat())
            spawnSmokeCloud(location = target, renderLocation = target, palette = AppState.options.burn.palette.burn)
        }
    )


    CustomItemRegistry.items += CustomItem(
        id = "burn",
        defaultItem = createNamedItem(Material.BLAZE_ROD, "Burn"),
        onRightClick = { player ->
            val target = getTarget(player) ?: return@CustomItem
            playSound(target, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, Random.nextDouble(1.0,1.5).toFloat())
            val burner = Burner(AppState.options.burn)
            burner.burn(block = target.block, renderLocation = target, heat = .5f)
        }
    )
}