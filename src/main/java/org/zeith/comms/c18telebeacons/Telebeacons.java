package org.zeith.comms.c18telebeacons;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.comms.c18telebeacons.blocks.redirector.BlockRedirector;
import org.zeith.comms.c18telebeacons.client.ClientProxy;
import org.zeith.comms.c18telebeacons.common.CommonProxy;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

@Mod(Telebeacons.MOD_ID)
public class Telebeacons
{
	public static final Logger LOG = LogManager.getLogger("Telebeacons");
	public static final String MOD_ID = "telebeacons";
	public static final CommonProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID)
	{
		@Override
		public ItemStack makeIcon()
		{
			return new ItemStack(BlockRedirector.REDIRECTOR);
		}
	};

	public Telebeacons()
	{
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigsTB.commonSpec);
		LanguageAdapter.registerMod(MOD_ID);
		PROXY.construct();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientSetup(FMLClientSetupEvent e)
	{
		((ClientProxy)PROXY).clientSetup();
	}

	@SubscribeEvent
	public void finish(FMLLoadCompleteEvent e)
	{
	}
}