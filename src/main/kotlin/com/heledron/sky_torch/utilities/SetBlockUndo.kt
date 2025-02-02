package com.heledron.sky_torch.utilities

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


private val savedData = mutableMapOf<Block, BlockData>()
fun setBlock(block: Block, data: BlockData) {
    saveAdjacentBlocks(block)
    block.setBlockData(data, false)
}

fun setBlock(block: Block, data: Material) {
    saveAdjacentBlocks(block)
    block.setType(data, false)
}

fun undoSetBlock() {
    for ((block, data) in savedData) {
        block.setBlockData(data, false)
    }
    savedData.clear()
}

private fun saveAdjacentBlocks(block: Block) {
    saveBlock(block)
    saveBlock(block.getRelative(0, 1, 0))
    saveBlock(block.getRelative(0, -1, 0))
    saveBlock(block.getRelative(1, 0, 0))
    saveBlock(block.getRelative(-1, 0, 0))
    saveBlock(block.getRelative(0, 0, 1))
    saveBlock(block.getRelative(0, 0, -1))
}

private fun saveBlock(block: Block) {
    if (block !in savedData) savedData[block] = block.blockData
}