package com.heledron.sky_torch.laser

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Waterlogged
import kotlin.random.Random

enum class BlockPalette(
    val burn: BurnPalette,
) {
    ORANGE(burn = BurnPalette.orange),
    BLUE(burn = BurnPalette.blue),
    PURPLE(burn = BurnPalette.purple),
    GLASS_ONLY(burn = BurnPalette().apply { burnWave = listOf(dataOf(Material.ORANGE_STAINED_GLASS)) }),
    ORANGE_WITHOUT_TRAIL(burn = BurnPalette().apply { burnWave = this.burnWave.map { i -> listOf(i[0])} }),
    ;
}

private fun <T> List<T>.repeat(count: Int) = List(count) { this }.flatten()

private fun repeat(count: Int, material: Material): Array<Material> = Array(count) { material }

private fun dataOf(vararg materials: Material) = materials.map { it.createBlockData() }

class BurnPalette {
    companion object {
        val orange = BurnPalette()

        val blue = BurnPalette().apply {
            burnWave = orange.burnWave + dataOf(
                *repeat(3, Material.BLUE_STAINED_GLASS),
                *repeat(2, Material.LAPIS_BLOCK),
                Material.BLUE_TERRACOTTA,
                Material.BLUE_CONCRETE,
                Material.TUBE_CORAL_BLOCK,
            ).map { listOf(it, it, it) + burnWaveTrail }.repeat(2)

            val blocks = listOf(Material.BLUE_CONCRETE, Material.TUBE_CORAL_BLOCK, Material.CRYING_OBSIDIAN)
            preBurn = orange.preBurn.repeat(4 * blocks.size) + blocks.map { it.createBlockData() }
            midBurn = orange.midBurn.repeat(4 * blocks.size) + blocks.map { it.createBlockData() }
        }
        val purple = BurnPalette().apply {
            burnWave = orange.burnWave + dataOf(
                *repeat(2, Material.PURPLE_STAINED_GLASS),
                *repeat(2, Material.CHORUS_PLANT),
                *repeat(2, Material.AMETHYST_BLOCK),
                Material.PURPLE_TERRACOTTA,
                Material.PURPLE_CONCRETE,
                Material.BUBBLE_CORAL_BLOCK,
            ).map { listOf(it, it, it) + burnWaveTrail }.repeat(2)

            val blocks = listOf(Material.PURPLE_CONCRETE, Material.AMETHYST_BLOCK, Material.CRYING_OBSIDIAN)
            preBurn = orange.preBurn.repeat(4 * blocks.size) + blocks.map { it.createBlockData() }
            midBurn = orange.midBurn.repeat(4 * blocks.size) + blocks.map { it.createBlockData() }
        }
    }

    var burnWave = dataOf(
        *repeat(3, Material.ORANGE_STAINED_GLASS),
        *repeat(2, Material.SHROOMLIGHT),
        Material.ORANGE_TERRACOTTA,
        Material.ORANGE_CONCRETE,
        Material.HONEYCOMB_BLOCK,
    ).map { listOf(it, it, it) + burnWaveTrail }

    var firePalette = listOf(Material.FIRE)

    var preBurn = dataOf(Material.ORANGE_TERRACOTTA)
    var midBurn = dataOf(Material.MAGMA_BLOCK, Material.ORANGE_TERRACOTTA)

    var midHeatPalette = dataOf(Material.MAGMA_BLOCK)
    var highHeat = dataOf(Material.MAGMA_BLOCK, Material.BLACKSTONE, Material.MUD, Material.TUFF)

    fun burn(block: Block, heat: Float): List<Pair<Long, BlockData>>? {
        fun applyHeat(list: List<BlockData>, heat: Float): List<BlockData> {
            if (heat > .9) return highHeat
            if (heat < .2) return list
            return list + midHeatPalette
        }

        fun rand (a: Long, b: Long) = Random.nextLong(a, b)

        if (block.type == Material.SHORT_GRASS) return listOf(
            rand(1, 10) to shortGrassPalette.random(),
        )

        if (Tag.LOGS_THAT_BURN.isTagged(block.type)) return listOf(
            rand(0, 1) to preBurn.random(),
            rand(1, 10) to midBurn.random(),
            rand(1 * 20, 10 * 20) to Material.POLISHED_BASALT.createBlockData().apply { block.blockData.copyTo(this) },
        )

        if (Tag.LEAVES.isTagged(block.type)) return listOf(
            rand(1, 10) to preBurn.random(),
            rand(1, 10) to midBurn.random(),
            rand(10, 80) to leavesPalette.random(),
        )

        if (block.type == Material.STONE_BRICK_STAIRS) return listOf(
            rand(10, 60) to stoneBrickStairsPalette.random().clone().apply { block.blockData.copyTo(this) },
        )

        if (block.type == Material.STONE_BRICK_SLAB) return listOf(
            rand(10, 60) to stoneBrickSlabPalette.random().clone().apply { block.blockData.copyTo(this) },
        )

        if (block.type in listOf(Material.GRASS_BLOCK, Material.DIRT, Material.SNOW_BLOCK, Material.SAND, Material.PODZOL, Material.COARSE_DIRT)) return listOf(
            rand(1, 5) to midBurn.random(),
            rand(10, 60) to applyHeat(grassPalette, heat).random()
        )

        if (block.type in listOf(Material.STONE, Material.ANDESITE, Material.GRAVEL, Material.COBBLESTONE)) return listOf(
            rand(1, 5) to midBurn.random(),
            rand(10, 60) to applyHeat(stonePalette, heat).random(),
        )

        if (block.type in listOf(Material.DARK_PRISMARINE)) return listOf(
            rand(10, 60) to applyHeat(darkPrismarinePalette, heat).random(),
        )

        if (listOf(Tag.WOODEN_FENCES, Tag.WOODEN_DOORS, Tag.WOODEN_TRAPDOORS).any { it.isTagged(block.type) }) {
            if (Random.nextFloat() < .2) return null
            return listOf(
                rand(10, 60) to Material.AIR.createBlockData(),
            )
        }

        if (!block.type.isSolid) return listOf(
            rand(1, 10) to Material.AIR.createBlockData(),
        )

        return null
    }


    fun smokePalette(): List<BlockData> {
        if (Random.nextFloat() < .25) return dataOf(
            Material.SHROOMLIGHT,
            Material.ORANGE_CONCRETE,
            Material.ORANGE_TERRACOTTA,
            listOf(Material.BLACK_CONCRETE, Material.ORANGE_TERRACOTTA).random(),
        )

        return dataOf(
            Material.SHROOMLIGHT,
            Material.ORANGE_CONCRETE,
            Material.ORANGE_STAINED_GLASS,
            Material.BLACK_STAINED_GLASS,
        )
    }
}

private val burnWaveTrail = dataOf(Material.ORANGE_STAINED_GLASS, Material.BLACK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS)

private val shortGrassPalette = dataOf(
    *repeat(4, Material.DEAD_BUSH),
    *repeat(8, Material.DEAD_BUSH),
    Material.DEAD_BRAIN_CORAL_FAN, Material.DEAD_BRAIN_CORAL,
    Material.DEAD_BUBBLE_CORAL_FAN, //Material.DEAD_BUBBLE_CORAL,
    Material.DEAD_FIRE_CORAL_FAN, Material.DEAD_FIRE_CORAL,
    Material.DEAD_HORN_CORAL_FAN, //Material.DEAD_HORN_CORAL,
    Material.DEAD_TUBE_CORAL_FAN, //Material.DEAD_TUBE_CORAL,
).map { (it as? Waterlogged)?.isWaterlogged = false; it  }

private val grassPalette = dataOf(
    *repeat(4, Material.COARSE_DIRT),
    *repeat(4, Material.ROOTED_DIRT),
    *repeat(2, Material.TUFF),
    Material.DEAD_HORN_CORAL_BLOCK, Material.DEAD_FIRE_CORAL_BLOCK, //Material.DEAD_BUBBLE_CORAL_BLOCK, Material.DEAD_BRAIN_CORAL_BLOCK,
)

private val stonePalette = dataOf(
    *repeat(3, Material.TUFF),
    Material.ANDESITE,
    Material.DEAD_HORN_CORAL_BLOCK, //Material.DEAD_FIRE_CORAL_BLOCK, Material.DEAD_BUBBLE_CORAL_BLOCK, Material.DEAD_BRAIN_CORAL_BLOCK,
    Material.DEEPSLATE,
)

private val leavesPalette = dataOf(
    Material.MANGROVE_ROOTS,
    *repeat(5, Material.AIR)
)

private val stoneBrickSlabPalette = dataOf(
    *repeat(15, Material.STONE_BRICK_SLAB),
    Material.ANDESITE_SLAB,
    Material.COBBLESTONE_SLAB,
    Material.TUFF_SLAB,
)

private val stoneBrickStairsPalette = dataOf(
    *repeat(15, Material.STONE_BRICK_STAIRS),
    Material.ANDESITE_STAIRS,
    Material.COBBLESTONE_STAIRS,
    Material.TUFF_STAIRS,
)

private val darkPrismarinePalette = dataOf(
    *repeat(15, Material.DARK_PRISMARINE),
    Material.DEEPSLATE,
    Material.COBBLED_DEEPSLATE,
)

