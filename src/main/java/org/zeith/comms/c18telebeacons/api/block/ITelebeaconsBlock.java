package org.zeith.comms.c18telebeacons.api.block;

import net.minecraft.item.ItemGroup;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.hammerlib.api.blocks.IItemGroupBlock;

public interface ITelebeaconsBlock
		extends IItemGroupBlock
{
	@Override
	default ItemGroup getItemGroup()
	{
		return Telebeacons.ITEM_GROUP;
	}
}