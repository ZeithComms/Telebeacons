package org.zeith.comms.c18telebeacons.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WhooshParticle
		extends SpriteTexturedParticle
{
	private final IAnimatedSprite sprite;

	protected WhooshParticle(IAnimatedSprite sprite, ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		this.sprite = sprite;
		this.setSpriteFromAge(sprite);
	}

	public WhooshParticle(IAnimatedSprite sprite, ClientWorld world, double x, double y, double z, double moveX, double moveY, double moveZ)
	{
		super(world, x, y, z, moveX, moveY, moveZ);
		this.sprite = sprite;
		this.setSpriteFromAge(sprite);
		this.xd = moveX;
		this.yd = moveY;
		this.zd = moveZ;
	}

	@Override
	public void tick()
	{
		super.tick();
		if(this.age < this.lifetime)
			this.setSpriteFromAge(this.sprite);
	}

	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory
			implements IParticleFactory<WhooshParticleData>
	{
		private final IAnimatedSprite sprite;

		public Factory(IAnimatedSprite sprite)
		{
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(WhooshParticleData type, ClientWorld world, double x, double y, double z, double mx, double my, double mz)
		{
			WhooshParticle p = new WhooshParticle(sprite, world, x, y, z, mx, my, mz);
			p.rCol = type.getR();
			p.gCol = type.getG();
			p.bCol = type.getB();
			return p;
		}
	}
}