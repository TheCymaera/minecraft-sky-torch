package com.heledron.sky_torch.laser

import com.heledron.sky_torch.AppState
import com.heledron.sky_torch.GameObject
import com.heledron.sky_torch.utilities.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

class LaserOptions {
    var duration = (20 * 1.5).toLong()

    var beamStartFrames = 4
    var beamEndFrames = beamStartFrames * 4

    var randomnessMagnitude = .5
    var randomnessPeriod = 12
    var flySpeed = 6.0 * 1.2

    var beamWidth = .8
    var glowWidthMin = beamWidth - .1
    var glowWidthMax = beamWidth + .5
    var glowPeriod = 3

    var applyNightVision = true

    var boreRadius = 3.0
    var boreBurnRadius = 5.0
    var boreDistance = 50

    var burn = BurnOptions()
    var shockwave = ShockWaveOptions()
    var burnWave = BurnWaveOptions()
    var flashBurn = FlashBurnOptions()
    var blinding = BlindingEffectOptions()

    var explodePlacementOffset = 2.0
    var flashBurnPlacementOffset = 5.0
    var digDepth = 1.0
}


fun spawnLaserWithChargeUp(placement: LaserPlacement, options: LaserOptions) {
    val scheduler = SeriesScheduler()

    // Target lock
    scheduler.run{ playSoundAtPlayers(placement.world, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0f) }

    // Charge up
    scheduler.sleep(20)
    scheduler.run{
        playSoundAtPlayers(placement.world, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1f, .8f)
        playSoundAtPlayers(placement.world, Sound.BLOCK_CONDUIT_AMBIENT, 1f, 0f)
    }

    // Fire
    scheduler.sleep(20)
    scheduler.run { Laser(placement, options) }
}


class LaserPlacement (
    val world: World,
    val hit: Vector,
    val origin: Vector,
    val velocityDirection: Vector,
    val render: Vector,
)

class Laser(private val placement: LaserPlacement, private val options: LaserOptions): GameObject() {
    private val onHit = EventEmitter()
    private val onRetractEnd = EventEmitter()
    private val burner = Burner(options.burn)

    override fun remove() {
        burner.close()
        onEnd()
        super.remove()
    }

    private fun onEnd() {
        // remove night vision
        for (player in placement.world.players) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION)
        }
    }

    init {
        runLater(options.duration) {
            retracting = true
        }

        onRetractEnd.listen {
            onEnd()
            // wait for burning to finish
            runLater(20 * 60) { remove() }
        }

        val explodeOrigin = placement.hit.clone()
        explodeOrigin.add(placement.hit.clone().subtract(placement.origin).normalize().multiply(options.explodePlacementOffset))
        val radiationOrigin = explodeOrigin.clone().add(Vector(.0, options.flashBurnPlacementOffset, .0))

        placement.hit.y -= options.digDepth


        onHit.listen {
            // give night vision
            if (options.applyNightVision) for (player in placement.world.players) {
                player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 1, true, false, false))
            }

            // blinding effect
            BlindEffect(placement.world.players, options.blinding)

            runLater(1) {
                playSoundAtPlayers(placement.world, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 0f)
                playSoundAtPlayers(placement.world, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f)
                playSoundAtPlayers(placement.world, Sound.ITEM_TOTEM_USE, 1f, .5f)
            }

            run {
                val placement = ShockWavePlacement(world = placement.world, origin = explodeOrigin, render = placement.render)
                spawnShockwave(placement, options.shockwave, burner)
            }
            runLater(1) {
                val placement = BurnWavePlacement(world = placement.world, origin = explodeOrigin, render = placement.render)
                spawnBurnWave(placement, options.burnWave, burner)
            }
            runLater(3) {
                val placement = FlashBurnPlacement(world = placement.world, origin = radiationOrigin, render = placement.render)
                flashBurn(placement, options.flashBurn, burner)
            }
        }
    }


    // state
    private var retracting = false
    private var startFrame = 0
    private var endTime = 0
    private var time = 0
    private var driftTimeX = .0
    private var driftTimeZ = .0
    private val originalHitLocation = placement.hit.clone()
    private var currentGlowWidth = 2.0

    override fun update() {
        if (endTime > options.beamEndFrames) return

        time++
        driftTimeX += Random.nextDouble(2.0)
        driftTimeZ += Random.nextDouble(2.2)

        placement.origin.add(placement.velocityDirection.clone().multiply(options.flySpeed))

        placement.hit.x = originalHitLocation.x + sin(driftTimeX / options.randomnessPeriod * Math.PI) * options.randomnessMagnitude
        placement.hit.z = originalHitLocation.z + sin(driftTimeZ / options.randomnessPeriod * Math.PI) * options.randomnessMagnitude

        startFrame++
        if (startFrame == options.beamStartFrames) onHit.emit()

        if (startFrame >= options.beamStartFrames) boreHole()

        if (retracting) {
            endTime++
            if (endTime == options.beamEndFrames) onRetractEnd.emit()
        }

        val glowSin = sin(time.toDouble() / options.glowPeriod * Math.PI) / 2 + .5
        currentGlowWidth = options.glowWidthMin + (options.glowWidthMax - options.glowWidthMin) * glowSin

    }

    private fun boreHole() {
        if (options.boreRadius == .0) return

        val tip = laserTip()
        val end = laserEnd()
        val stride = end.clone().subtract(tip)
        if (stride.isZero) return
        stride.normalize().multiply(options.boreRadius)

        val tries = ceil(options.boreDistance / options.boreRadius).toInt()

        for (i in 0 until tries) {
            val position = tip.clone().add(stride.clone().multiply(i))

            for (offset in sphereBlockOffsets(ceil(options.boreRadius).toInt())) {
                val block = position.toLocation(placement.world).block.getRelative(offset.x, offset.y, offset.z)
                setBlock(block, Material.AIR)
            }

            for (offset in sphereBlockOffsets(ceil(options.boreBurnRadius).toInt())) {
                val block = position.toLocation(placement.world).block.getRelative(offset.x, offset.y, offset.z)
                burner.burn(block, placement.render.toLocation(placement.world), heat = 1.0f)
            }
        }
    }

    override fun render() {
        AppState.renderer.render(this, getModel())
    }

    private fun laserTip() = placement.origin.clone().lerp(placement.hit, (startFrame.toDouble() / options.beamStartFrames).coerceAtMost(1.0));
    private fun laserEnd() = placement.origin.clone().lerp(placement.hit, (endTime.toDouble() / options.beamEndFrames).coerceAtMost(1.0));

    private fun getModel(): Model {
        val model = Model()

        val from = laserTip()
        val to = laserEnd()
        val vector = to.clone().subtract(from)

        if (vector.isZero) return model

        val direction = vector.clone().normalize()
        val upVector = direction.clone().crossProduct(Vector(0, 1, 0)).normalize()

        model.add("beam", lineModel(
            renderLocation = placement.render.toLocation(placement.world),
            location = from.clone().add(direction.clone().multiply(-20)).toLocation(placement.world),
            vector = vector,
            upVector = upVector,
            thickness = options.beamWidth.toFloat(),
            interpolation = 1,
            init = {
                it.block = Material.SMOOTH_QUARTZ.createBlockData()
                it.brightness = Display.Brightness(15, 15)
            },
        ))

        model.add("glow", lineModel(
            renderLocation = placement.render.toLocation(placement.world),
            location = from.clone().add(direction.clone().multiply(-20 + currentGlowWidth)).toLocation(placement.world),
            vector = vector,
            upVector = upVector,
            thickness = currentGlowWidth.toFloat(),
            interpolation = 1,
            init = {
                it.block = Material.WHITE_STAINED_GLASS.createBlockData()
                it.brightness = Display.Brightness(15, 15)
            }
        ))

        return model
    }
}