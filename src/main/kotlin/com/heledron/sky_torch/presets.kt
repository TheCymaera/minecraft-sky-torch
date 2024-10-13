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

fun presetShockwaveOnly() {
    AppState.options.boreDistance = 0

    AppState.options.flySpeed = .0
    AppState.options.randomnessMagnitude = .0

    AppState.options.burnWave.count = 0
    AppState.options.burnWave.delayedCount = 0
    AppState.options.flashBurn.horizontalCount = 0

    AppState.options.shockwave = default.shockwave
}



fun presetStage1() {
    presetDefault()

    // show laser and disable everything else
    AppState.options.duration = 10000000

    AppState.options.boreDistance = 0

    AppState.options.flySpeed = .0
    AppState.options.randomnessMagnitude = .0

    AppState.options.burnWave.count = 0
    AppState.options.burnWave.delayedCount = 0
    AppState.options.burnWave.burnBlocks = 0
    AppState.options.shockwave.count = 0
    AppState.options.flashBurn.horizontalCount = 0

    AppState.options.applyNightVision = false
}

fun presetStage2() {
    presetStage1()

    // show radiation wave
    AppState.options.duration = default.duration
    AppState.options.flashBurn.horizontalCount = default.flashBurn.horizontalCount
    AppState.options.flashBurn.debugDuration = 20 * 5
}

fun presetStage3() {
    presetStage2()
    AppState.options.flashBurn.debugDuration = 0

    // show burn wave
    AppState.options.burnWave.count = 80
    AppState.options.burnWave.delayedCount = 0
    AppState.options.burnWave.durationMax = 20 * 20
    AppState.options.burnWave.durationMin = AppState.options.burnWave.durationMax - 1
    AppState.options.burnWave.disableRotation = true
    AppState.options.burnWave.disableSmartRendering = true
    AppState.options.burnWave.palette = BlockPalette.GLASS_ONLY
    AppState.options.burnWave.airDragCoefficient = .0
}

fun presetStage4() {
    presetStage3()

    // add random rotation
    AppState.options.burnWave.disableRotation = false
}

fun presetStage5() {
    presetStage4()

    // use better block palette
    AppState.options.burnWave.palette = BlockPalette.ORANGE_WITHOUT_TRAIL
}

fun presetStage6() {
    presetStage5()

    // use smart rendering
    AppState.options.burnWave.disableSmartRendering = false
}

fun presetStage7() {
    presetStage6()

    // limit reach
    AppState.options.burnWave.durationMax = default.burnWave.durationMax
    AppState.options.burnWave.durationMin = default.burnWave.durationMin
    AppState.options.burnWave.airDragCoefficient = default.burnWave.airDragCoefficient
}

fun presetStage8() {
    presetStage7()

    // use animated palette
    AppState.options.burnWave.palette = BlockPalette.ORANGE
}

fun presetStage9() {
    presetStage8()

    // burn blocks
    AppState.options.burnWave.disableHeat = true
    AppState.options.burnWave.burnBlocks = default.burnWave.burnBlocks
}

fun presetStage10() {
    presetStage9()

    // enable shockwave
    AppState.options.shockwave.count = default.shockwave.count
    AppState.options.shockwave.cameraShake.magnitude = .0
    AppState.options.shockwave.destructionRadius = .0
    AppState.options.shockwave.flyingBlockChance = .0
}

fun presetStage11() {
    presetStage10()

    // enable camera shake
    AppState.options.shockwave.cameraShake = default.shockwave.cameraShake
}

fun presetStage12() {
    presetStage11()

    // enable destruction
    AppState.options.shockwave.destructionRadius = default.shockwave.destructionRadius
}

fun presetStage13() {
    presetStage12()

    // enable flying blocks
    AppState.options.shockwave.flyingBlockChance = default.shockwave.flyingBlockChance
}


fun presetStage14() {
    presetStage13()

    // delayed explosions
    AppState.options.burnWave = default.burnWave
}

fun presetStage15() {
    presetStage14()

    // laser movement
    AppState.options.flySpeed = default.flySpeed
    AppState.options.randomnessMagnitude = default.randomnessMagnitude
}

fun presetStage16() {
    presetStage15()

    // night vision
    AppState.options.applyNightVision = true
}

fun presetStage17() {
    // default
    presetDefault()
}