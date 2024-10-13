package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.utilities.lineModel
import com.heledron.sky_torch.utilities.onTick
import com.heledron.sky_torch.utilities.raycastGround
import com.heledron.sky_torch.utilities.runLater
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class FlashBurnOptions {
    var horizontalCount = 40
    var verticalCount = 10
    var verticalMinAngle = -Math.PI / 2 * .6
    var verticalMaxAngle = Math.PI / 2 * .6

    var rayDistance = 100.0

    var debugDuration = 0
}

class FlashBurnPlacement(
    val world: World,
    val origin: Vector,
    val render: Vector
)

fun flashBurn(placement: FlashBurnPlacement, options: FlashBurnOptions, burner: Burner) {
    val rays = sphereRays(
        horizontalCount = options.horizontalCount,
        vertCount = options.verticalCount,
        vertMinAngle = options.verticalMinAngle,
        vertMaxAngle = options.verticalMaxAngle,
    )


    for (direction in rays) {
        val hit = raycastGround(placement.origin.toLocation(placement.world), direction, options.rayDistance)?.hitPosition?.toLocation(placement.world)


        if (options.debugDuration != 0) spawnDebugGraphics(placement = placement, options = options, direction = direction, hit = hit?.toVector())

        if (hit == null) continue

        val area = 2
        for (x in -area..area) for (z in -area..area) for (y in -area..area) {
            if (Random.nextBoolean()) continue

            val block = hit.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block

            burner.burn(block = block, renderLocation = placement.render.toLocation(placement.world), heat = .5f)
        }
    }
}

private fun sphereRays(
    horizontalCount: Int,
    vertMinAngle: Double,
    vertMaxAngle: Double,
    vertCount: Int
) = iterator {
    val horizontalStep = Math.PI * 2 / horizontalCount
    val verticalStep = (vertMaxAngle - vertMinAngle) / vertCount

    for (horizontalIndex in 0 until horizontalCount) {
        val horizontalAngle = horizontalIndex * horizontalStep

        for (verticalIndex in 0 until vertCount) {
            val verticalAngle = vertMinAngle + verticalIndex * verticalStep

            yield(Vector(
                cos(horizontalAngle) * cos(verticalAngle),
                sin(verticalAngle),
                sin(horizontalAngle) * cos(verticalAngle)
            ))
        }
    }
}


private fun spawnDebugGraphics(placement: FlashBurnPlacement, options: FlashBurnOptions, direction: Vector, hit: Vector?) {
    val vector = hit?.subtract(placement.origin) ?: direction.clone().multiply(options.rayDistance)
    val blockData = if (hit == null) Material.LIGHT_GRAY_STAINED_GLASS.createBlockData() else Material.RED_STAINED_GLASS.createBlockData()

    runLater(Random.nextLong(5)) {
        onTick {
            AppState.renderer.render(direction, lineModel(
                location = placement.origin.toLocation(placement.world),
                vector = vector,
                renderLocation = placement.render.toLocation(placement.world),
                thickness = .2f,
                interpolation = 1,
                init = {
                    it.block = blockData
                    it.brightness = Display.Brightness(15, 15)
                }
            ))
        }.let {
            val duration = 20 * 3 + Random.nextLong(20 * 2)
            runLater(duration) { it.close() }
        }
    }
}
