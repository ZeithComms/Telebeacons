package org.zeith.comms.c18telebeacons.init;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlocksTB
{
	public static boolean never(BlockState p_235427_0_, IBlockReader p_235427_1_, BlockPos p_235427_2_, EntityType<?> p_235427_3_)
	{
		return false;
	}

	public static boolean never(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_)
	{
		return false;
	}

	public static boolean always(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_)
	{
		return true;
	}
}