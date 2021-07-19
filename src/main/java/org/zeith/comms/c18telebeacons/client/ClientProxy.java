package org.zeith.comms.c18telebeacons.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.blocks.redirector.BlockRedirector;
import org.zeith.comms.c18telebeacons.blocks.transceiver.BlockTransceiver;
import org.zeith.comms.c18telebeacons.client.model.BakedModelRedirector;
import org.zeith.comms.c18telebeacons.client.particles.WhooshParticle;
import org.zeith.comms.c18telebeacons.client.particles.WhooshParticleType;
import org.zeith.comms.c18telebeacons.client.render.TESRTransceiver;
import org.zeith.comms.c18telebeacons.common.CommonProxy;

import java.util.ArrayList;
import java.util.List;

public class ClientProxy
		extends CommonProxy
{
	public static final List<ResourceLocation> CANCEL_LOAD_MODEL = new ArrayList<>();

	@Override
	public void construct()
	{
		super.construct();

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::$1);
		modBus.addListener(this::$2);
		modBus.addListener(this::$3);
	}

	public void clientSetup()
	{
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, Flash::renderGameOverlay);
		MinecraftForge.EVENT_BUS.addListener(Flash::clientTick);
		RenderTypeLookup.setRenderLayer(BlockTransceiver.TRANSCEIVER, RenderType.cutout());
	}

	@Override
	public boolean isClientPlayer(Entity e)
	{
		return e == Minecraft.getInstance().player;
	}

	@Override
	public void excludeModelState(BlockState state)
	{
		CANCEL_LOAD_MODEL.add(BlockModelShapes.stateToModelLocation(state));
	}

	public void $1(ModelRegistryEvent e)
	{
		ModelLoader.addSpecialModel(TESRTransceiver.SPINNY);
	}

	public void $2(ModelBakeEvent e)
	{
		AtlasTexture blocksAtlas = e.getModelManager().getAtlas(PlayerContainer.BLOCK_ATLAS);

		TextureAtlasSprite open = blocksAtlas.getSprite(new ResourceLocation(Telebeacons.MOD_ID, "block/redirector_open"));
		TextureAtlasSprite closed = blocksAtlas.getSprite(new ResourceLocation(Telebeacons.MOD_ID, "block/redirector_closed"));

		for(BlockState state : BlockRedirector.REDIRECTOR.getStateDefinition().getPossibleStates())
			e.getModelRegistry().put(BlockModelShapes.stateToModelLocation(state),
					new BakedModelRedirector(false, true, true, open, closed));
	}

	public void $3(ParticleFactoryRegisterEvent e)
	{
		Minecraft.getInstance().particleEngine.register(WhooshParticleType.WHOOSH, WhooshParticle.Factory::new);
	}
}