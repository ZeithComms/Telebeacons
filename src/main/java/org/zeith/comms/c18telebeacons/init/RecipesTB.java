package org.zeith.comms.c18telebeacons.init;

import net.minecraftforge.common.Tags;
import org.zeith.comms.c18telebeacons.blocks.redirector.BlockRedirector;
import org.zeith.comms.c18telebeacons.blocks.transceiver.BlockTransceiver;
import org.zeith.hammerlib.annotations.ProvideRecipes;
import org.zeith.hammerlib.api.IRecipeProvider;
import org.zeith.hammerlib.event.recipe.RegisterRecipesEvent;

@ProvideRecipes
public class RecipesTB
	implements IRecipeProvider
{
	@Override
	public void provideRecipes(RegisterRecipesEvent event)
	{
		event.shaped()
				.id(BlockRedirector.REDIRECTOR.getRegistryName())
				.result(BlockRedirector.REDIRECTOR, 1)
				.shape("qpq", "prp", "qpq")
				.map('q', Tags.Items.GEMS_QUARTZ)
				.map('p', Tags.Items.GLASS_PANES)
				.map('r',  Tags.Items.GEMS_PRISMARINE)
				.register();

		event.shaped()
				.id(BlockTransceiver.TRANSCEIVER.getRegistryName())
				.result(BlockTransceiver.TRANSCEIVER, 1)
				.shape("ggg", "gpg", "qeq")
				.map('g', Tags.Items.GLASS)
				.map('p',  Tags.Items.GEMS_PRISMARINE)
				.map('q', Tags.Items.STORAGE_BLOCKS_QUARTZ)
				.map('e',  Tags.Items.ENDER_PEARLS)
				.register();
	}
}