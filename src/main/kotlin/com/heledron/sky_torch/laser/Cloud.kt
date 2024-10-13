package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.GameObject
import com.heledron.sky_torch.utilities.*
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import org.joml.Matrix4f
import kotlin.random.Random

class Cloud(
    val location: Location,
    val renderLocation: Location,
    val velocity: Vector,
    var size: Double,
    val growth: Double,
    val maxAge: Int,
    val blocks: List<BlockData>,
    var disableSmartRendering: Boolean = false,
): GameObject() {
    var age = 0

    var pitch = Random.nextFloat() * 360f
    var yaw = Random.nextFloat() * 360f

    var pitchVelocity = Random.nextFloat() * 0.01f
    var yawVelocity = Random.nextFloat() * 0.01f

    var onUpdate = {}

    init {
        runLater(maxAge.toLong()) { remove() }
    }


    override fun update() {
        onUpdate()

        size += growth
        location.add(velocity)

        pitch += pitchVelocity
        yaw += yawVelocity

        age += 1
    }

    override fun render() {
        AppState.renderer.render(this, getModel())
    }

    private fun getSmartRenderLocation(): Location {
        return if (disableSmartRendering) location else renderLocation
    }

    private fun getModel() = blockModel(
        location = getSmartRenderLocation(),
        init = {
            it.brightness = Display.Brightness(15, 15)
            it.teleportDuration = 1
            it.interpolationDuration = 1
//            it.viewRange = 3000f
        },
        update = {
            val index = (age.toFloat() / maxAge * blocks.size).toInt().coerceAtMost(blocks.size - 1)

//            val location = location.clone().add(velocity.clone().normalize().multiply(size / 2))
            val diff = location.toVector().subtract(getSmartRenderLocation().toVector())

            it.block = blocks[index]

            val finalSize = size.toFloat()

            val matrix = Matrix4f()
                .translate(diff.x.toFloat(), diff.y.toFloat(), diff.z.toFloat())
                .rotateXYZ(pitch, yaw, 0f)
                .translate(-finalSize / 2, -finalSize / 2, 0f)
                .scale(finalSize, finalSize, finalSize)

            applyTransformationWithInterpolation(it, matrix)
        }
    )
}