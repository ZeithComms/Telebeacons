package org.zeith.comms.c18telebeacons.client.particles;

import com.mojang.serialization.Codec;
import net.minecraft.particles.ParticleType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public class WhooshParticleType
		extends ParticleType<WhooshParticleData>
{
	@RegistryName("whoosh")
	public static final WhooshParticleType WHOOSH = new WhooshParticleType(false);

	public WhooshParticleType(boolean overrideLimiter)
	{
		super(overrideLimiter, WhooshParticleData.DESERIALIZER);
	}

	@Override
	public Codec<WhooshParticleData> codec()
	{
		return WhooshParticleData.CODEC;
	}
}