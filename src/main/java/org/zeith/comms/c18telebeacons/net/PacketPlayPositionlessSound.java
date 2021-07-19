package org.zeith.comms.c18telebeacons.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class PacketPlayPositionlessSound
		implements IPacket
{
	private SoundEvent sound;
	private SoundCategory source;
	private float pitch;

	public PacketPlayPositionlessSound()
	{
	}

	public PacketPlayPositionlessSound(SoundEvent snd, SoundCategory cat, float pitch)
	{
		Validate.notNull(snd, "sound");
		this.sound = snd;
		this.source = cat;
		this.pitch = pitch;
	}

	@Override
	public void read(PacketBuffer buf)
	{
		this.sound = Registry.SOUND_EVENT.byId(buf.readVarInt());
		this.source = buf.readEnum(SoundCategory.class);
		this.pitch = buf.readFloat();
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
		buf.writeEnum(this.source);
		buf.writeFloat(this.pitch);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(sound, pitch));
	}
}