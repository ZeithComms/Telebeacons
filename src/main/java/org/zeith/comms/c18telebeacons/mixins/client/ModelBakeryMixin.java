package org.zeith.comms.c18telebeacons.mixins.client;

import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comms.c18telebeacons.client.ClientProxy;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin
{
	@Inject(
			method = "loadModel",
			at = @At("HEAD"),
			cancellable = true
	)
	public void injectLoadModel(ResourceLocation rl, CallbackInfo ci)
	{
		if(rl.getClass() == ModelResourceLocation.class && ClientProxy.CANCEL_LOAD_MODEL.contains(rl))
			ci.cancel();
	}
}