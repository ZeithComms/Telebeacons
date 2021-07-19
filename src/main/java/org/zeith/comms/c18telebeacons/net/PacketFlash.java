package org.zeith.comms.c18telebeacons.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c18telebeacons.client.Flash;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class PacketFlash
	implements IPacket
{
	private int fadeIn, stay, fadeOut;

	public PacketFlash(int fadeIn, int stay, int fadeOut)
	{
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	public PacketFlash()
	{
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeInt(fadeIn);
		buf.writeInt(stay);
		buf.writeInt(fadeOut);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		fadeIn = buf.readInt();
		stay = buf.readInt();
		fadeOut = buf.readInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		Flash.flashScreen(fadeIn, stay, fadeOut);
	}
}