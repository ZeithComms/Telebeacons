package org.zeith.comms.c18telebeacons.client.render;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.blocks.transceiver.BlockTransceiver;
import org.zeith.comms.c18telebeacons.blocks.transceiver.TileTransceiver;
import org.zeith.comms.c18telebeacons.client.Flash;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.hammerlib.client.render.tile.ITESR;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderState.*;

public class TESRTransceiver
		implements ITESR<TileTransceiver>
{
	public static final ResourceLocation SPINNY = new ResourceLocation(Telebeacons.MOD_ID, "block/spinny");

	static final float[] ONE = new float[]{
			1,
			1,
			1
	};

	public static final RenderType LINES = RenderType.create("lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.builder().setLineState(new RenderState.LineState(OptionalDouble.of(10.0))).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	@Override
	public void render(TileTransceiver tile, float partial, MatrixStack matrix, IRenderTypeBuffer buf, int lighting, int overlay, TileEntityRendererDispatcher renderer)
	{
		Minecraft mc = Minecraft.getInstance();

		IBakedModel model = mc.getModelManager().getModel(SPINNY);

		BeaconBeam beam = tile.beam.get();
		World world = tile.getLevel();
		long time = world != null ? world.getGameTime() : 0L;
		TESRRedirector.renderBeam(matrix, buf, beam, partial, time);

		matrix.translate(8 / 16F, 8 / 16F, 8 / 16F);
		matrix.mulPose(tile.getBlockState().getValue(BlockTransceiver.FACING).getRotation());

		if(beam != null)
		{
			float flash = Flash.getFlash(MathHelper.lerp(partial, tile.prevFlash, tile.nowFlash), 10, 15, 23);
			float progress = MathHelper.lerp(partial, tile.prevOpen, tile.nowOpen) / 10F;
			if(progress > 0.001F || flash > 0)
			{
				matrix.pushPose();
				matrix.translate(-8 / 16F, -8 / 16F, -8 / 16F);
				float[] rgb = beam.endColor;
				if(rgb == null) rgb = ONE;
				progress *= 0.999f;

				if(progress > 0)
				{
					AxisAlignedBB tpBox = new AxisAlignedBB(-1.499 * progress, 2.001, -1.499 * progress, 1.499 * progress, 1.99 + 3 * progress, 1.499 * progress);
					IVertexBuilder lines = buf.getBuffer(LINES);
					WorldRenderer.renderLineBox(matrix, lines, tpBox.move(0.5, 0.001, 0.5), rgb[0], rgb[1], rgb[2], 1F);
				}

				if(flash > 0)
				{
					AxisAlignedBB tpBox = new AxisAlignedBB(-1.499, 2.001, -1.499, 1.499, 1.99 + 3, 1.499).move(0.5, 0.001, 0.5);
					IVertexBuilder lightning = buf.getBuffer(RenderType.lightning());
					Matrix4f pose = matrix.last().pose();
					for(int i = 0; i < 6; ++i)
					{
						tpBox = tpBox.deflate(1.25 / 6);
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.minY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.maxZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.maxX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
						lightning.vertex(pose, (float) tpBox.minX, (float) tpBox.maxY, (float) tpBox.minZ).color(1F, 1F, 1F, flash).endVertex();
					}
				}

				matrix.popPose();
			}
		}

		matrix.mulPose(Vector3f.YP.rotationDegrees(tile.spinny.getActualRotation(partial)));
		matrix.translate(-8 / 16F, -8 / 16F, -8 / 16F);

		matrix.translate(0, 3 / 16F, 0);
		BlockModelRenderer mr = mc.getBlockRenderer().getModelRenderer();

		IVertexBuilder solid = buf.getBuffer(emissive());
		mr.renderModel(tile.getLevel(), model, tile.getBlockState(), tile.getBlockPos(), matrix, solid, false, tile.getRNG(), tile.getBlockState().getSeed(tile.getBlockPos()), overlay, EmptyModelData.INSTANCE);

		if(beam != null)
		{
			IVertexBuilder glint = buf.getBuffer(RenderType.glintDirect());
			mr.renderModel(tile.getLevel(), model, tile.getBlockState(), tile.getBlockPos(), matrix, glint, false, tile.getRNG(), tile.getBlockState().getSeed(tile.getBlockPos()), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
		}
	}

	@Override
	public boolean shouldRenderOffScreen(TileTransceiver tile)
	{
		return true;
	}

	private static final RenderType EMISSIVE = RenderType.create(
			"emissive",
			DefaultVertexFormats.BLOCK,
			7,
			256,
			true,
			false,
			RenderType.State.builder()
					.setShadeModelState(SMOOTH_SHADE)
					.setLightmapState(NO_LIGHTMAP)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.createCompositeState(true)
	);

	public static RenderType emissive()
	{
		return EMISSIVE;
	}
}