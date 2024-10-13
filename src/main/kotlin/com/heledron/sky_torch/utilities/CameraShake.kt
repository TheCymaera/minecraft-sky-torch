package com.heledron.sky_torch.utilities

import org.bukkit.entity.Player
import java.io.Closeable
import kotlin.math.cos
import kotlin.math.sin

class CameraShakeOptions (
    var magnitude: Double,
    var decay: Double,
    var pitchPeriod: Double,
    var yawPeriod: Double,
)

class CameraShake(
    player: Player,
    options: CameraShakeOptions,
): Closeable {
    private var time = 0
    private var prevPitch = .0
    private var prevYaw = .0

    private var magnitude = options.magnitude

    private val repeat = interval(1,1) {
        time += 1

        magnitude -= options.decay

        if (magnitude < 0) {
            close()
            return@interval
        }


        val pitch = sin(time.toDouble() / options.pitchPeriod * 2 * Math.PI) * magnitude
        val yaw = cos(time.toDouble() / options.yawPeriod * 2 * Math.PI) * magnitude

        val relativePitch = pitch - prevPitch
        val relativeYaw = yaw - prevYaw

        prevPitch = pitch
        prevYaw = yaw

        val command = String.format("execute as %s at @s run tp @s ~ ~ ~ ~%.3f ~%.3f", player.uniqueId, relativePitch, relativeYaw)
        runCommandSilently(command)
    }

    override fun close() {
        repeat.close()
    }
}