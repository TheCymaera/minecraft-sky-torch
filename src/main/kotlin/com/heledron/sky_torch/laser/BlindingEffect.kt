package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.GameObject
import com.heledron.sky_torch.utilities.textModel
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.joml.Matrix4f
import org.joml.Quaternionf


class BlindingEffectOptions(
    var enabled: Boolean = false,
    var color: Color = Color.WHITE,
    var fadeInDuration: Int = 3,
    var duration: Int = 20,
    var fadeOutDuration: Int = 10,
)

class BlindEffect(private val players: List<Player>, val options: BlindingEffectOptions) : GameObject() {
    private var age = 0
    override fun update() {
        age++

        if (age > options.fadeInDuration + options.duration + options.fadeOutDuration) {
            remove()
        }
    }

    override fun render() {
        if (!options.enabled) return

        val opacity = when {
            age < options.fadeInDuration -> age.toDouble() / options.fadeInDuration
            age < options.fadeInDuration + options.duration -> 1.0
            else -> 1 - (age - options.fadeInDuration - options.duration).toDouble() / options.fadeOutDuration
        }

        if (opacity <= 0) return

        val color = options.color.setAlpha((options.color.alpha * opacity).toInt())

        for (player in players) {
            for ((i,transform) in transforms.withIndex()) AppState.renderer.render(this to player to i, textModel(
                location = player.eyeLocation.toVector().toLocation(player.world),
                //location = player.eyeLocation,
                init = {
                    it.text = " "
                    it.brightness = Display.Brightness(15, 15)
                    it.teleportDuration = 1
                    it.setTransformationMatrix(transform)
                },
                update = {
                    // TODO: Remove when this bug is resolved
                    // https://bugs.mojang.com/browse/MC-259812
//                    it.isSeeThrough = if (opacity < 1) true else false
                    it.backgroundColor = color
                }
            ))
        }

    }
}


private val transforms = listOf(
    Quaternionf(),
    Quaternionf().rotateY(Math.PI.toFloat() / 2 * 1),
    Quaternionf().rotateY(Math.PI.toFloat() / 2 * 2),
    Quaternionf().rotateY(Math.PI.toFloat() / 2 * 3),

    Quaternionf().rotateX(Math.PI.toFloat() / 2),
    Quaternionf().rotateX(-Math.PI.toFloat() / 2),
).map {
    val size = 2.5f
    Matrix4f()
        .rotate(it)
        .scale(size,size,1f)
        .translate(-.5f, -.5f, -size / 2)
        .mul(textBackgroundTransform)
}


val textBackgroundTransform: Matrix4f; get() = Matrix4f()
    .translate(-0.1f + .5f,-0.5f + .5f,0f)
    .scale(8.0f,4.0f,1f) //  + 0.003f  + 0.001f