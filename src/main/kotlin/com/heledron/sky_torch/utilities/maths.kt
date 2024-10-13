package com.heledron.sky_torch.utilities

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.sign

val DOWN_VECTOR; get () = Vector(0, -1, 0)

val UP_VECTOR; get () = Vector(0, 1, 0)

fun Vector.lerp(target: Vector, factor: Double): Vector {
    this.add(target.clone().subtract(this).multiply(factor))
    return this
}

fun Vector.moveTowards(target: Vector, constant: Double) {
    val diff = target.clone().subtract(this)
    val distance = diff.length()
    if (distance <= constant) {
        this.copy(target)
    } else {
        this.add(diff.multiply(constant / distance))
    }
}

fun Location.moveTowards(target: Location, constant: Double) {
    val vector = this.toVector()
    vector.moveTowards(target.toVector(), constant)
    this.x = vector.x
    this.y = vector.y
    this.z = vector.z
}

fun Double.lerp(target: Double, factor: Double): Double {
    return this + (target - this) * factor
}

fun Double.moveTowards(target: Double, speed: Double): Double {
    val distance = target - this
    return if (abs(distance) < speed) target else this + speed * distance.sign
}

fun randomDirection(): Vector {
    return Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize()
}