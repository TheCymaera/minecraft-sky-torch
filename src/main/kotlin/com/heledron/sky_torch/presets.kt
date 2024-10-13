package com.heledron.sky_torch

import com.heledron.sky_torch.laser.BlockPalette
import com.heledron.sky_torch.laser.LaserOptions

private val default = LaserOptions()

fun presetDefault() {
    AppState.options = LaserOptions()
}

fun presetOrange() {
    AppState.options.burnWave.palette = BlockPalette.ORANGE
    AppState.options.burn.palette = BlockPalette.ORANGE
}

fun presetBlue() {
    AppState.options.burnWave.palette = BlockPalette.BLUE
    AppState.options.burn.palette = BlockPalette.BLUE
}

fun presetPurple() {
    AppState.options.burnWave.palette = BlockPalette.PURPLE
    AppState.options.burn.palette = BlockPalette.PURPLE
}

fun presetEnableFlicker() {
    AppState.options.glowWidthMin = default.glowWidthMin
}

fun presetDisableFlicker() {
    AppState.options.glowWidthMin = AppState.options.glowWidthMax
}

fun presetBig() {
    presetSetScale(1.0)

    val options = AppState.options
//    options.beamWidth = 2.2
//    options.glowWidthMin = options.beamWidth - .1
//    options.glowWidthMax = options.beamWidth * 1.5

    options.shockwave.destructionRadius = 7.6
    options.shockwave.destructionRange = 40.0

    options.burnWave.growth = 0.5
    options.burnWave.burnBlocks = 4
    options.burnWave.speedMin = 1.3
    options.burnWave.speedMax = options.burnWave.speedMin * 1.07
    options.burnWave.scanDownwards = 5.0
}

fun presetSetScale(scale: Double) {
    val options = AppState.options

    options.beamWidth = default.beamWidth * scale
    options.glowWidthMin = default.glowWidthMin * scale
    options.glowWidthMax = default.glowWidthMax * scale
    options.boreRadius = default.boreRadius * scale
    options.boreBurnRadius = default.boreBurnRadius * scale
    options.explodePlacementOffset = default.explodePlacementOffset * scale
    options.flashBurnPlacementOffset = default.flashBurnPlacementOffset * scale
    options.digDepth = default.digDepth * scale

    options.burnWave.size = default.burnWave.size * scale
    options.burnWave.growth = default.burnWave.growth * scale
    options.shockwave.destructionRadius = default.shockwave.destructionRadius * scale
    options.shockwave.destructionRange = default.shockwave.destructionRange * scale

    options.burnWave.size = default.burnWave.size * scale
    options.burnWave.growth = default.burnWave.growth * scale
    options.burnWave.burnBlocks = 4
    options.burnWave.speedMin = default.burnWave.speedMin * scale
    options.burnWave.speedMax = default.burnWave.speedMax * scale
}


fun presetShockwaveVisualizer() {
//    AppState.options.duration = 10000000

    AppState.options.boreDistance = 0

    AppState.options.flySpeed = .0
    AppState.options.randomnessMagnitude = .0

    AppState.options.burnWave.count = 0
    AppState.options.burnWave.delayedCount = 0
    AppState.options.flashBurn.horizontalCount = 0

    AppState.options.shockwave = default.shockwave
    AppState.options.shockwave.speed = default.shockwave.speed / 10
    AppState.options.shockwave.growth = default.shockwave.growth / 10
    AppState.options.shockwave.duration = 20 * 30
    AppState.options.shockwave.count = 10
    AppState.options.shockwave.markDestroyedBlocks = true
    AppState.options.shockwave.cameraShake.magnitude = 0.0
    AppState.options.shockwave.flyingBlockChance = 0.0
}