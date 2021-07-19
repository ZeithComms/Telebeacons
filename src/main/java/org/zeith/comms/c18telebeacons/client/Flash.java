package org.zeith.comms.c18telebeacons.client;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2IntArrayMap;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.net.PacketFlash;
import org.zeith.comms.c18telebeacons.net.PacketPlayPositionlessSound;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.net.Network;

@SimplyRegister
public class Flash
{
	private static int clientFlashTime, clientFlashTimeFadeIn, clientFlashTimeStay, clientFlashTimeFadeOut, clientFlashTimeTotal;

	private static Long2IntArrayMap beaconFlashes = new Long2IntArrayMap();

	@RegistryName("flash")
	public static final SoundEvent FLASH = new SoundEvent(new ResourceLocation(Telebeacons.MOD_ID, "flash"));

	@OnlyIn(Dist.CLIENT)
	public static void flashBeaconNetwork(BlockPos beacon)
	{
		beaconFlashes.put(beacon.asLong(), 1);
	}

	@OnlyIn(Dist.CLIENT)
	public static float getFlash(float clientFlashTime, int clientFlashTimeFadeIn, int clientFlashTimeStay, int clientFlashTimeFadeOut)
	{
		int clientFlashTimeTotal = clientFlashTimeFadeIn + clientFlashTimeFadeOut + clientFlashTimeStay;
		if(clientFlashTime > 0 && clientFlashTime < clientFlashTimeTotal)
		{
			if(clientFlashTime < clientFlashTimeFadeIn)
				return Math.min(clientFlashTime / (float) clientFlashTimeFadeIn, 1F);

			if(clientFlashTime < clientFlashTimeStay)
				return 1F;

			if(clientFlashTime < clientFlashTimeStay + clientFlashTimeFadeOut)
			{
				float left = clientFlashTime - clientFlashTimeStay;
				left = clientFlashTimeFadeOut - left;
				return Math.max(0, left / (float) clientFlashTimeFadeOut);
			}
		}

		return 0F;
	}

	@OnlyIn(Dist.CLIENT)
	public static float getFlash(float partialTicks)
	{
		if(clientFlashTime > 0 && clientFlashTime < clientFlashTimeTotal)
		{
			if(clientFlashTime < clientFlashTimeFadeIn)
				return Math.min((clientFlashTime + partialTicks) / (float) clientFlashTimeFadeIn, 1F);

			if(clientFlashTime < clientFlashTimeStay)
				return 1F;

			if(clientFlashTime < clientFlashTimeStay + clientFlashTimeFadeOut)
			{
				float left = (clientFlashTime + partialTicks) - clientFlashTimeStay;
				left = clientFlashTimeFadeOut - left;
				return Math.max(0, left / (float) clientFlashTimeFadeOut);
			}
		}

		return 0F;
	}

	@OnlyIn(Dist.CLIENT)
	public static float getBeamFlash(BlockPos beaconPos, float partialTicks)
	{
		long bp = beaconPos.asLong();
		return beaconFlashes.containsKey(bp) ? getFlash(beaconFlashes.get(bp) + partialTicks, 5, 15, 23) : 0;
	}

	public static void flashScreen(int fadeIn, int stay, int fadeOut)
	{
		clientFlashTime = 0;
		clientFlashTimeFadeIn = fadeIn;
		clientFlashTimeStay = fadeIn + stay;
		clientFlashTimeFadeOut = fadeOut;
		clientFlashTimeTotal = clientFlashTimeFadeIn + clientFlashTimeFadeOut + clientFlashTimeStay;
	}

	public static void tpFlash(Entity entity)
	{
		flashScreenFor(entity, 3, 15, 30);
	}

	public static void flashScreenFor(Entity entity, int fadeIn, int stay, int fadeOut)
	{
		if(entity instanceof ServerPlayerEntity)
			flashScreenForPlayer((ServerPlayerEntity) entity, fadeIn, stay, fadeOut);
		if(Telebeacons.PROXY.isClientPlayer(entity))
			flashScreen(fadeIn, stay, fadeOut);
	}

	public static void flashScreenForPlayer(ServerPlayerEntity player, int fadeIn, int stay, int fadeOut)
	{
		Network.sendTo(new PacketFlash(fadeIn, stay, fadeOut), player);
		Network.sendTo(new PacketPlayPositionlessSound(FLASH, SoundCategory.BLOCKS, 1F), player);
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientTick(TickEvent.ClientTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			if(clientFlashTimeTotal > 0)
			{
				++clientFlashTime;
				if(clientFlashTime > clientFlashTimeTotal)
				{
					clientFlashTimeTotal = 0;
					clientFlashTime = 0;
				}
			}

			for(long l : beaconFlashes.keySet()) beaconFlashes.put(l, beaconFlashes.get(l) + 1);
			beaconFlashes.values().removeIf((int i) -> i >= 44);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderGameOverlay(RenderGameOverlayEvent.Pre e)
	{
		RenderGameOverlayEvent.ElementType et = e.getType();
		if(clientFlashTime > 0 && clientFlashTime <= clientFlashTimeTotal && et != RenderGameOverlayEvent.ElementType.VIGNETTE)
		{
			if(et == RenderGameOverlayEvent.ElementType.ALL)
			{
				// render flash
				MainWindow mw = e.getWindow();

				int x = 0, y = 0, width = mw.getScreenWidth(), height = mw.getScreenHeight();
				float r = 1F, g = 1F, b = 1F, a = getFlash(e.getPartialTicks());

				RenderSystem.enableBlend();

				Tessellator tess = Tessellator.getInstance();
				BufferBuilder vb = tess.getBuilder();
				vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				vb.vertex(x, y + height, 0).color(r, g, b, a).endVertex();
				vb.vertex(x + width, y + height, 0).color(r, g, b, a).endVertex();
				vb.vertex(x + width, y, 0).color(r, g, b, a).endVertex();
				vb.vertex(x, y, 0).color(r, g, b, a).endVertex();
				tess.end();

				RenderSystem.disableBlend();
			} else
				e.setCanceled(true);
		}
	}
}