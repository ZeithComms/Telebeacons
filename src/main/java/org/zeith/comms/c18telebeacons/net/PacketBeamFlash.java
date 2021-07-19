package org.zeith.comms.c18telebeacons.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c18telebeacons.client.Flash;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class PacketBeamFlash
		implements IPacket
{
	public BlockPos beaconPos;

	public PacketBeamFlash()
	{
	}

	public PacketBeamFlash(BlockPos beaconPos)
	{
		this.beaconPos = beaconPos;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(beaconPos);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		beaconPos = buf.readBlockPos();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		Flash.flashBeaconNetwork(beaconPos);
	}
}