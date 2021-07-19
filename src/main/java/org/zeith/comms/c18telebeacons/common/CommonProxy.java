package org.zeith.comms.c18telebeacons.common;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

public class CommonProxy
{
	public void construct()
	{
	}

	public void excludeModelState(BlockState state)
	{
	}

	public boolean isClientPlayer(Entity e)
	{
		return false;
	}
}