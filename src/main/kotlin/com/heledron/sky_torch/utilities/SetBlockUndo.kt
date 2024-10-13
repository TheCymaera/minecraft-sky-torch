package com.heledron.sky_torch.utilities

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


val savedData = mutableMapOf<Block, BlockData>()
fun setBlock(block: Block, data: BlockData) {
    saveAdjacentBlocks(block)
    if (!isInLoadedChunk(block)) return
    block.setBlockData(data, false)
}

fun setBlock(block: Block, data: Material) {
    saveAdjacentBlocks(block)
    if (!isInLoadedChunk(block)) return
    block.setType(data, false)
}

fun undoSetBlock() {
    for ((block, data) in savedData) {
        block.setBlockData(data, false)
    }
    savedData.clear()
}

private fun isInLoadedChunk(block: Block): Boolean {
    return block.chunk.isLoaded
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
    if (!isInLoadedChunk(block)) return
    if (block !in savedData) savedData[block] = block.blockData
}