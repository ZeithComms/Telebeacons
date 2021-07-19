package org.zeith.comms.c18telebeacons.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.zeith.comms.c18telebeacons.blocks.redirector.BlockRedirector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BakedModelRedirector
		implements IDynamicBakedModel
{
	protected final boolean hasAmbientOcclusion;
	protected final boolean isGui3d;
	protected final boolean usesBlockLight;
	protected final TextureAtlasSprite open, closed;

	private static final FaceBakery FACE_BAKERY = new FaceBakery();

	public BakedModelRedirector(boolean ao, boolean bl, boolean i3d, TextureAtlasSprite pO, TextureAtlasSprite pC)
	{
		this.hasAmbientOcclusion = ao;
		this.isGui3d = i3d;
		this.usesBlockLight = bl;
		this.open = pO;
		this.closed = pC;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(state != null && side != null)
		{
			boolean open = BlockRedirector.isSideOpen(state, side);

			return Collections.singletonList(FACE_BAKERY.bakeQuad(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16), new BlockPartFace(side, 0, "", new BlockFaceUV(new float[]{
					0,
					0,
					16,
					16
			}, 0)), open ? this.open : closed, side, ModelRotation.X0_Y0, null, false, BlockRedirector.REDIRECTOR.getRegistryName()));
		}

		return Collections.emptyList();
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return hasAmbientOcclusion;
	}

	@Override
	public boolean isGui3d()
	{
		return isGui3d;
	}

	@Override
	public boolean usesBlockLight()
	{
		return usesBlockLight;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return open;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}
}
