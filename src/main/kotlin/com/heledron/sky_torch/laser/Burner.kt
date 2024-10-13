package com.heledron.sky_torch.laser

import com.heledron.sky_torch.utilities.SeriesScheduler
import com.heledron.sky_torch.utilities.randomDirection
import com.heledron.sky_torch.utilities.runLater
import com.heledron.sky_torch.utilities.setBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockSupport
import org.bukkit.block.data.Waterlogged
import java.io.Closeable
import kotlin.math.abs
import kotlin.random.Random

class BurnOptions {
    var disabled = false
    var setFireChance = 1.0 / 80
    var spawnSmokeChance = 0.2

    var palette = BlockPalette.ORANGE
}

class Burner(val options: BurnOptions): Closeable {
    var isClosed = false
    val visited = mutableSetOf<Block>()
    var burning = mutableMapOf<Block, Material>()

    override fun close() {
        isClosed = true
    }

    fun burn(block: Block, renderLocation: Location, heat: Float) {
        val palette = options.palette.burn

        if (isOccluded(block)) return

        if (block in visited) return
        visited += block

        if (options.disabled) return

        if (block.isLiquid || hasWater(block)) return

        val blockBelow = block.getRelative(0, -1, 0)
        if (block.type.isAir && canPlaceFireOn(blockBelow)) {
            if (Random.nextFloat() > options.setFireChance) return

            setBlock(block, palette.firePalette.random())

            // wait a while and check if the fire is still in a valid position
            runLater(20 * 10) {
                if (isClosed) return@runLater
                if (!canPlaceFireOn(blockBelow)) setBlock(block, Material.AIR)
            }

            return
        }

        if (block.type.isAir) return

        val burnPalette = palette.burn(block, heat) ?: return

        val series = SeriesScheduler()
        burning[block] = block.type
        for ((delay, data) in burnPalette) {
            series.sleep(delay)
            series.run {
                if (isClosed) return@run
                if (block.type.isAir)  return@run
                setBlock(block, data)
            }
        }

        if (Random.nextFloat() < options.spawnSmokeChance) spawnSmokeCloud(
            location = block.location,
            renderLocation = renderLocation,
            palette = palette
        )
    }


    private fun isOccluded(block: Block): Boolean {
        fun isOccluding(block: Block) = (burning[block]?: block.type).isOccluding
        return isOccluding(block.getRelative(0, 1, 0)) &&
                isOccluding(block.getRelative(0, -1, 0)) &&
                isOccluding(block.getRelative(1, 0, 0)) &&
                isOccluding(block.getRelative(-1, 0, 0)) &&
                isOccluding(block.getRelative(0, 0, 1)) &&
                isOccluding(block.getRelative(0, 0, -1))
    }
}

private fun canPlaceFireOn(block: Block) = block.blockData.isFaceSturdy(BlockFace.UP, BlockSupport.RIGID)

fun hasWater(block: Block): Boolean {
    if (block.type == Material.KELP) return true
    if (block.type == Material.KELP_PLANT) return true
    if (block.type == Material.SEAGRASS) return true
    if (block.type == Material.TALL_SEAGRASS) return true
    return (block.blockData as? Waterlogged)?.isWaterlogged == true
}

fun spawnSmokeCloud(location: Location, renderLocation: Location, palette: BurnPalette) = Cloud(
    location = location.clone(),
    renderLocation = renderLocation,
    velocity = randomDirection().apply { y = abs(y) }.normalize().multiply(1.0 / 10),
    size = 0.1,
    growth = Random.nextDouble(0.01, 0.05),
    maxAge = Random.nextDouble(1.5 * 20, 2.5 * 20).toInt(),
    blocks = palette.smokePalette(),
).apply {
    onUpdate = {
        val airDragCoefficient = 0.02
        velocity.x *= 1 - airDragCoefficient
        velocity.z *= 1 - airDragCoefficient
    }
}