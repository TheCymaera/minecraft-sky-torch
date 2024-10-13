package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.GameObject
import com.heledron.sky_torch.utilities.*
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Vector
import org.joml.Matrix4f
import kotlin.random.Random



class FlyingBlock(
    val renderLocation: Location,
    val location: Location,
    val velocity: Vector,
    var blockData: BlockData,
): GameObject() {
    val pitch = Random.nextDouble(2 * Math.PI).toFloat()
    val yaw = Random.nextDouble(2 * Math.PI).toFloat()
    val rotateAxis = velocity.clone().crossProduct(Vector(0, 1, 0)).normalize().toVector3f()
    val rotateSpeed = Random.nextDouble(0.1, 0.8)
    var rotation = .0

    val gravityAcceleration = .08
    val airDragCoefficient = .02

    init {
        runLater(20 * 10) { remove() }
    }

    override fun update() {
        if (velocity.y < -10 && location.block.type.isOccluding) {
            remove()
        }

        // apply gravity and air resistance
        velocity.y -= gravityAcceleration
        velocity.multiply(1 - airDragCoefficient)

        // apply rotation
        rotation += rotateSpeed

        location.add(velocity)
    }

    override fun render() {
        AppState.renderer.render(this, getModel())
    }

    fun getModel(): ModelPart<BlockDisplay> = blockModel(
        location = renderLocation,
        init = {
            it.teleportDuration = 1
            it.interpolationDuration = 1
        },
        update = {
            val diff = location.toVector().subtract(renderLocation.toVector())

            it.block = blockData

            val finalSize = 1f

            val matrix = Matrix4f()
                .translate(diff.x.toFloat(), diff.y.toFloat(), diff.z.toFloat())
                .rotate(rotation.toFloat(), rotateAxis)
                .rotateXYZ(pitch, yaw, 0f)
                .translate(-finalSize / 2, -finalSize / 2, 0f)
                .scale(finalSize, finalSize, finalSize)

            applyTransformationWithInterpolation(it, matrix)
        }
    )
}