package com.heledron.sky_torch.laser

import com.heledron.sky_torch.utilities.*
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.random.Random

class BurnWaveOptions {
    var count = 20
    var delayedCount = 90
    var durationMin = (4.0 * 20).toInt()
    var durationMax = (8.0 * 20).toInt()

    var maxSpawnDelay = 35L
    var spawnRandomInterval = 5

    var growth = .25
    var size = .1
    var burnBlocks = 4
//    var maxBurnBlocks = 800

    var speedMin = .5 * 1.5
    var speedMax = .55 * 1.5

    var palette = BlockPalette.ORANGE

    var scanUpwards = 25.0
    var scanDownwards = 0.0

    var airDragCoefficient = .005


    var disableHeat = false
}

class BurnWavePlacement(
    val world: World,
    val origin: Vector,
    val render: Vector
)


fun spawnBurnWave(placement: BurnWavePlacement, options: BurnWaveOptions, burn: Burner) {
    for ((x, y) in ring(options.count)) spawnBurnCloud(placement, options, burn, x, y)

    for ((x, y) in ring(options.delayedCount)) runLater(Random.nextLong(options.maxSpawnDelay / options.spawnRandomInterval) * options.spawnRandomInterval) {
        spawnBurnCloud(placement, options, burn, x, y)
    }



//    runLater(Random.nextLong(options.maxSpawnDelay / options.spawnRandomInterval) * options.spawnRandomInterval) {  }
}

private fun spawnBurnCloud(placement: BurnWavePlacement, options: BurnWaveOptions, burn: Burner, x: Double, y: Double) = Cloud(
    location = placement.origin.toLocation(placement.world),
    velocity = Vector(x, .1, y).normalize().multiply(Random.nextDouble(options.speedMin,options.speedMax)),
    size = options.size,
    growth = options.growth,
    maxAge = Random.nextInt(options.durationMin, options.durationMax),
    blocks = options.palette.burn.burnWave.random(),
    renderLocation = placement.render.toLocation(placement.world),
).apply {
//    val affectedPlayers = mutableListOf<org.bukkit.entity.Player>()

    onUpdate = {
//        val location = location.clone().add(velocity.clone().normalize().multiply(size / 2))

        velocity.multiply(1 - options.airDragCoefficient)

//        // check if players are within the cloud
//        for (player in location.world!!.players) {
//            if (player in affectedPlayers) continue
//            if (player.location.distanceSquared(location) > (size * 1.5).pow(2)) continue
//
//            affectedPlayers += player
//
////            CameraShake(player = player, options = CameraShakeOptions(
////                magnitude = .25,
////                decay = 0.02,
////                pitchPeriod = 3.7,
////                yawPeriod = 3.0,
////            ))
//        }



        val scanUpwards = size / 2 + options.scanUpwards
        val scanDownwards = size / 2 + options.scanDownwards
        val top = location.clone().add(.0,scanUpwards,.0)
        val ground = raycastGround(top, Vector(0, -1, 0), scanUpwards + scanDownwards)?.hitPosition?.toLocation(location.world!!) ?: location.clone()

        if (location.block.type.isOccluding) {
            location.y = location.y.lerp(ground.y + size / 2, .03)
        }

        val burnHeight = (size * .7).coerceAtLeast(5.0)
        val burnAmount = size.coerceAtLeast(1.0) * options.burnBlocks
        repeat(burnAmount.toInt()) {
            val block = ground.clone().block.getRelative(
                Random.nextInt(-3, 3),
                Random.nextDouble(-burnHeight, burnHeight).toInt(),
                Random.nextInt(-3, 3),
            )

            val heat = if (options.disableHeat) .5f else 1f - (age.toFloat() / maxAge)

            burn.burn(block = block, renderLocation = renderLocation, heat = heat)
        }
    }
}