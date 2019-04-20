package me.deftware.client.framework.wrappers.world;

import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import javax.annotation.Nullable;

public class IBlock {

	protected Block block;

	public IBlock(String name) {
		block = Block.getBlockFromName(name);
	}

	public IBlock(Block block) {
		this.block = block;
	}

	public boolean isValidBlock() {
		return block != null;
	}

	public boolean isAir() {
		return block == Blocks.AIR;
	}

	public Block getBlock() {
		return block;
	}

	public int getID() {
		return Block.REGISTRY.getIDForObject(block);
	}

	public String getLocalizedName() {
		return block.getNameTextComponent().getUnformattedComponentText();
	}

	public String getTranslationKey() {
		return block.getTranslationKey();
	}

	public String getBlockKey() {
		return getTranslationKey().substring("block.minecraft.".length());
	}

	public static boolean isValidBlock(String name) {
		return Block.getBlockFromName(name) != null;
	}

	public static boolean isReplaceable(IBlockPos pos) {
		return IWorld.getStateFromPos(pos).getMaterial().isReplaceable();
	}

	public boolean instanceOf(IBlockTypes type) {
		if (type.equals(IBlockTypes.BlockContainer)) {
			return block instanceof BlockContainer;
		} else if (type.equals(IBlockTypes.BlockCrops)) {
			return block instanceof BlockCrops;
		} else if (type.equals(IBlockTypes.BlockPumpkin)) {
			return block instanceof BlockPumpkin;
		} else if (type.equals(IBlockTypes.BlockMelon)) {
			return block instanceof BlockMelon;
		} else if (type.equals(IBlockTypes.BlockReed)) {
			return block instanceof BlockReed;
		} else if (type.equals(IBlockTypes.BlockCactus)) {
			return block instanceof BlockCactus;
		} else if (type.equals(IBlockTypes.BlockNetherWart)) {
			return block instanceof BlockNetherWart;
		} else if (type.equals(IBlockTypes.BlockFarmland)) {
			return block instanceof BlockFarmland;
		} else if (type.equals(IBlockTypes.BlockSoulSand)) {
			return block instanceof BlockSoulSand;
		}
		return false;
	}

	public enum IBlockTypes {
		// Types
		BlockContainer, BlockCrops,

		// Specific blocks
		BlockPumpkin, BlockMelon, BlockReed, BlockCactus, BlockNetherWart, BlockFarmland, BlockSoulSand
	}

}
