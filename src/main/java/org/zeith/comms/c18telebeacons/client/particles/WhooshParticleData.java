package org.zeith.comms.c18telebeacons.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public class WhooshParticleData
		implements IParticleData
{
	public static final WhooshParticleData NO_COLOR = new WhooshParticleData(1.0F, 1.0F, 1.0F, 1.0F);

	public static final Codec<WhooshParticleData> CODEC = RecordCodecBuilder.create(builder ->
			builder.group(
					Codec.FLOAT.fieldOf("r").forGetter(data -> data.r),
					Codec.FLOAT.fieldOf("g").forGetter(data -> data.g),
					Codec.FLOAT.fieldOf("b").forGetter(data -> data.b),
					Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale))
					.apply(builder, WhooshParticleData::new)
	);

	public static final IParticleData.IDeserializer<WhooshParticleData> DESERIALIZER = new IParticleData.IDeserializer<WhooshParticleData>()
	{
		@Override
		public WhooshParticleData fromCommand(ParticleType<WhooshParticleData> type, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			float f = (float) reader.readDouble();
			reader.expect(' ');
			float f1 = (float) reader.readDouble();
			reader.expect(' ');
			float f2 = (float) reader.readDouble();
			reader.expect(' ');
			float f3 = (float) reader.readDouble();
			return new WhooshParticleData(f, f1, f2, f3);
		}

		@Override
		public WhooshParticleData fromNetwork(ParticleType<WhooshParticleData> type, PacketBuffer buf)
		{
			return new WhooshParticleData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
		}
	};

	private final float r;
	private final float g;
	private final float b;
	private final float scale;

	public WhooshParticleData(float r, float g, float b, float scale)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.scale = MathHelper.clamp(scale, 0.01F, 4.0F);
	}

	@Override
	public void writeToNetwork(PacketBuffer buf)
	{
		buf.writeFloat(this.r);
		buf.writeFloat(this.g);
		buf.writeFloat(this.b);
		buf.writeFloat(this.scale);
	}

	@Override
	public String writeToString()
	{
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.r, this.g, this.b, this.scale);
	}

	@Override
	public ParticleType<WhooshParticleData> getType()
	{
		return WhooshParticleType.WHOOSH;
	}

	@OnlyIn(Dist.CLIENT)
	public float getR()
	{
		return this.r;
	}

	@OnlyIn(Dist.CLIENT)
	public float getG()
	{
		return this.g;
	}

	@OnlyIn(Dist.CLIENT)
	public float getB()
	{
		return this.b;
	}

	@OnlyIn(Dist.CLIENT)
	public float getScale()
	{
		return this.scale;
	}

	public static WhooshParticleData create(float[] channel)
	{
		return channel == null || channel.length < 3 ? NO_COLOR : new WhooshParticleData(channel[0], channel[1], channel[2], 1F);
	}
}