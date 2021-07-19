package org.zeith.comms.c18telebeacons.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c18telebeacons.blocks.transceiver.TileTransceiver;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class PacketStartFlash
		implements IPacket
{
	BlockPos pos;

	public PacketStartFlash()
	{
	}

	public PacketStartFlash(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		World world = Minecraft.getInstance().level;
		if(world != null)
		{
			TileEntity te = world.getBlockEntity(pos);
			if(te instanceof TileTransceiver)
				((TileTransceiver) te).nowFlash = 1;
		}
	}
}