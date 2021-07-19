package org.zeith.comms.c18telebeacons.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comms.c18telebeacons.blocks.transceiver.BlockTransceiver;

import static org.zeith.comms.c18telebeacons.blocks.transceiver.TileTransceiver.TB_CD;
import static org.zeith.comms.c18telebeacons.blocks.transceiver.TileTransceiver.TB_CD_OWNER;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow
	public World level;

	@Shadow
	public abstract CompoundNBT getPersistentData();

	@Shadow
	public abstract AxisAlignedBB getBoundingBox();

	@Inject(
			method = "processPortalCooldown",
			at = @At("HEAD")
	)
	public void doCooldown(CallbackInfo ci)
	{
		CompoundNBT nbt = getPersistentData();

		int tbCd = nbt.getInt(TB_CD);

		if(nbt.contains(TB_CD_OWNER, Constants.NBT.TAG_LONG))
		{
			BlockPos owner = BlockPos.of(nbt.getLong(TB_CD_OWNER));
			AxisAlignedBB aabb = BlockTransceiver.getTeleportationBounds(level, owner);
			BlockState state;
			if(aabb != null && aabb.intersects(getBoundingBox()) && (state = level.getBlockState(owner)).getBlock() == BlockTransceiver.TRANSCEIVER && !(level.getBestNeighborSignal(owner) <= 0 || state.getValue(BlockStateProperties.INVERTED)))
				tbCd = Math.max(20, tbCd);
		}

		if(tbCd > 0)
		{
			--tbCd;
			if(tbCd <= 0)
			{
				nbt.remove(TB_CD_OWNER);
				nbt.remove(TB_CD);
			} else
				nbt.putInt(TB_CD, tbCd);
		}
	}
}