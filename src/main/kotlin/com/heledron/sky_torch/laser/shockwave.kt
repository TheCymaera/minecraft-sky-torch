package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.utilities.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

class ShockWaveOptions {
    var count = 120

    var cameraShake = CameraShakeOptions(
        magnitude = 1.2,
        decay = 0.04,
        pitchPeriod = 3.7,
        yawPeriod = 3.0,
    )

    var destructionRadius = 3.8
    var destructionCurve = 3
    var destructionRange = 20.0

    var flyingBlockChance = 0.1
    var flyingBlockMinVelocity = 2.7 * 1.2
    var flyingBlockMaxVelocity = 3.0 * 1.2

    var size = .1
    var growth = .125
    var duration = 4 * 20
    var speed = 3.2

    var scanUpwards = 15.0

    var markDestroyedBlocks = false
}

class ShockWavePlacement(
    val world: World,
    val origin: Vector,
    val render: Vector
)

val shockWavePalette = listOf(Material.WHITE_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS).map { it.createBlockData() }

fun spawnShockwave(placement: ShockWavePlacement, options: ShockWaveOptions, burner: Burner) {
    val visitedPlayers = mutableListOf<Player>()

    for ((x,z) in ring(options.count)) Cloud(
        renderLocation = placement.render.toLocation(placement.world),
        location = placement.origin.toLocation(placement.world),
        velocity = Vector(x, .02, z).multiply(options.speed),
        size = options.size,
        growth = options.growth,
        maxAge = options.duration,
        blocks = listOf(shockWavePalette.random()),
    ).apply {
        onUpdate = onUpdate@{
            val radiusSquared = location.toVector().add(velocity).distanceSquared(placement.origin)
            for (player in placement.world.players) {
                if (player in visitedPlayers) continue
                if (player.location.toVector().distanceSquared(placement.origin) > radiusSquared) continue
                visitedPlayers += player

                CameraShake(player = player, options = options.cameraShake)
            }

            val top = location.clone().add(.0,options.scanUpwards,.0)
            val ground = raycastGround(top, Vector(0, -1, 0), options.scanUpwards)?.hitPosition?.toLocation(location.world!!) ?: location

            if (location.block.type.isOccluding) {
                location.y = location.y.lerp(ground.y + size / 2, .1)
            }

            val radiusFraction = (radiusSquared / options.destructionRange.pow(2))

            if (radiusFraction > 1) return@onUpdate

            val removeRadius = ((1 - radiusFraction).pow(options.destructionCurve) * options.destructionRadius).roundToInt()
                .coerceAtLeast(if (options.destructionRadius == .0) 0 else 1) // remove at least one block, unless radius is 0

            if (removeRadius == 0) return@onUpdate

            for (offset in sphereBlockOffsets(removeRadius)) {
                val block = ground.block.getRelative(offset.x, offset.y, offset.z)

                if (options.markDestroyedBlocks) {
                    AppState.renderer.render(block, blockModel( // "shockwave" to x to z to offset
                        location = block.location.add(Vector(.5, .5, .5)),
                        init = {
                            it.block = Material.RED_CONCRETE.createBlockData()
                            it.transformation = centredTransform(1.0f, 1.0f, 1.0f)
//                            it.teleportDuration = 1
                        }
                    ))
                }

                if (block.type.isAir) continue
                if (block.isLiquid) continue

                if (Random.nextFloat() < options.flyingBlockChance) {
                    spawnFlyingBlock(block, velocity, radiusFraction, renderLocation, burner.options.palette.burn, options)
                }

                removeBlock(block)
            }
        }
    }
}

private fun removeBlock(block: Block) {
    if (hasWater(block)) {
        setBlock(block, Material.WATER)
    } else {
        setBlock(block, Material.AIR)
    }
}

private fun spawnFlyingBlock(block: Block, shockWaveVelocity: Vector, radiusFraction: Double, renderLocation: Location, burnPalette: BurnPalette, options: ShockWaveOptions) {
    val palette = burnPalette.burn(block, heat = 1.0f) ?: listOf(0L to block.blockData)
    val blockData = block.blockData

    val velocity = shockWaveVelocity.clone()
    val power = 1.0 - radiusFraction

    velocity.y = velocity.length() * power
    velocity.normalize()
    velocity.multiply(Random.nextDouble(options.flyingBlockMinVelocity, options.flyingBlockMaxVelocity) * power.pow(2))

    runLater(Random.nextLong(5)) {
        val flying = FlyingBlock(
            renderLocation = renderLocation,
            location = block.location,
            velocity = velocity,
            blockData = blockData,
        )

        val series = SeriesScheduler()

        for ((delay, data) in palette) {
            series.sleep(delay)
            series.run { flying.blockData = data }
        }
    }
}