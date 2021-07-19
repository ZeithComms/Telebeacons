package org.zeith.comms.c18telebeacons.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import org.zeith.comms.c18telebeacons.client.Flash;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.blocks.redirector.TileRedirector;
import org.zeith.hammerlib.client.render.tile.ITESR;

public class TESRRedirector
		implements ITESR<TileRedirector>
{
	@Override
	public void render(TileRedirector tile, float partial, MatrixStack matrix, IRenderTypeBuffer buf, int lighting, int overlay, TileEntityRendererDispatcher renderer)
	{
		BeaconBeam beam = tile.beam.get();
		World world = tile.getLevel();
		long time = world != null ? world.getGameTime() : 0L;
		renderBeam(matrix, buf, beam, partial, time);
	}

	@Override
	public boolean shouldRenderOffScreen(TileRedirector tile)
	{
		return true;
	}

	public static void renderBeam(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, BeaconBeam beam, float partialTicks, long totalWorldTime)
	{
		if(beam != null)
		{
			int sc = beam.getSegmentCount();
			for(int i = 0; i < sc; ++i)
			{
				BeaconBeam.BeaconBeamSegment segment = beam.getSegment(i);
				if(segment == null) break;

				renderBeamSegment(matrixStackIn, bufferIn, segment, beam.direction, partialTicks, totalWorldTime, beam.getStrengthF() * 0.75F + 0.25F + 0.75F * Flash.getBeamFlash(beam.beaconPos, partialTicks));
			}
		}
	}

	private static void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, BeaconBeam.BeaconBeamSegment segment, Vector3d direction, float partialTicks, long totalWorldTime, float scale)
	{
		renderBeamSegment(matrixStackIn, bufferIn, BeaconTileEntityRenderer.BEAM_LOCATION, segment, direction, partialTicks, 1.0F, totalWorldTime, 0.2F * scale, 0.25F * scale);
	}

	public static void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, ResourceLocation textureLocation, BeaconBeam.BeaconBeamSegment segment, Vector3d direction, float partialTicks, float textureScale, long totalWorldTime, float beamRadius, float glowRadius)
	{
		float height = (float) segment.length;
		float[] colors = segment.getColor();

		matrixStackIn.pushPose();
		matrixStackIn.translate(segment.offset.x, segment.offset.y, segment.offset.z); // offset by the correct distance

		matrixStackIn.mulPose(Vector3f.YP.rotation((float) (Math.atan2(direction.x, direction.z) + Math.PI)));
		matrixStackIn.mulPose(Vector3f.XP.rotation((float) (Math.asin(direction.y))));
		matrixStackIn.mulPose(Direction.NORTH.getRotation());

		float f = (float) Math.floorMod(totalWorldTime, 40L) + partialTicks;
		float f1 = height < 0 ? f : -f;
		float f2 = MathHelper.frac(f1 * 0.2F - (float) MathHelper.floor(f1 * 0.1F));
		float f3 = colors[0];
		float f4 = colors[1];
		float f5 = colors[2];

		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
		float f9 = -beamRadius;
		float f12 = -beamRadius;
		float f15 = -1.0F + f2;
		float f16 = height * textureScale * (0.5F / beamRadius) + f15;
		renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(textureLocation, false)), f3, f4, f5, 1.0F, height, 0.0F, beamRadius, beamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
		matrixStackIn.popPose();
		float f6 = -glowRadius;
		float f7 = -glowRadius;
		float f8 = -glowRadius;
		f9 = -glowRadius;
		f15 = -1.0F + f2;
		f16 = height * textureScale + f15;
		renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(textureLocation, true)), f3, f4, f5, 0.125F, height, f6, f7, glowRadius, f8, f9, glowRadius, glowRadius, glowRadius, 0.0F, 1.0F, f16, f15);
		matrixStackIn.popPose();
	}

	private static void renderPart(MatrixStack matrixStackIn, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float height, float p_228840_8_, float p_228840_9_, float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float u1, float u2, float v1, float v2)
	{
		MatrixStack.Entry matrixstack$entry = matrixStackIn.last();
		Matrix4f matrix4f = matrixstack$entry.pose();
		Matrix3f matrix3f = matrixstack$entry.normal();
		addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, 0, height, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, u1, u2, v1, v2);
		addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, 0, height, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, u1, u2, v1, v2);
		addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, 0, height, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, u1, u2, v1, v2);
		addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, 0, height, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, u1, u2, v1, v2);
	}

	private static void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float yMin, float yMax, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2)
	{
		addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x1, z1, u2, v1);
		addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x1, z1, u2, v2);
		addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x2, z2, u1, v2);
		addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x2, z2, u1, v1);
	}

	private static void addVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float y, float x, float z, float texU, float texV)
	{
		bufferIn.vertex(matrixPos, x, y, z)
				.color(red, green, blue, alpha)
				.uv(texU, texV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(matrixNormal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}
}