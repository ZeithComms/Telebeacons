package org.zeith.comms.c18telebeacons.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.zeith.comms.c18telebeacons.api.tile.IBeamAcceptor;
import org.zeith.comms.c18telebeacons.api.tile.IBranchedBeacon;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.common.mapping.BeaconMapping;
import org.zeith.comms.c18telebeacons.ConfigsTB;
import org.zeith.comms.c18telebeacons.utils.BlockPosUtils;

@Mixin(BeaconTileEntity.class)
@Implements({
		@Interface(iface = IBranchedBeacon.class, prefix = "IBranchedBeacon$")
})
public abstract class BeaconTileEntityMixin
		extends TileEntity
{
	@Shadow
	private int levels;

	@Shadow
	protected abstract void updateBase(int x, int y, int z);

	public BeaconTileEntityMixin(TileEntityType<?> type)
	{
		super(type);
	}

	boolean hasBranchedBeam;

	public boolean IBranchedBeacon$hasBranchedBeam()
	{
		return hasBranchedBeam;
	}

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBeaconColorMultiplier(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)[F"
			)
	)
	public float[] getBeaconColor(BlockState blockState, IWorldReader world, BlockPos pos, BlockPos beacon)
	{
		TileEntity tile = level.getBlockEntity(pos);
		if(tile instanceof IBeamAcceptor)
		{
			Vector3d start = new Vector3d(worldPosition.getX() + 0.5, worldPosition.getY() + 0.75, worldPosition.getZ() + 0.5);
			Vector3d end = new Vector3d(worldPosition.getX() + 0.5, pos.getY() + 0.9999, worldPosition.getZ() + 0.5);

			BlockRayTraceResult result = BlockPosUtils.clipOneBlock(level, pos, start, end);
			if(result.getType() == RayTraceResult.Type.MISS)
				return blockState.getBeaconColorMultiplier(world, pos, beacon);

			if(result.getType() == RayTraceResult.Type.BLOCK)
			{
				end = ((IBeamAcceptor) tile).handleEndPositioning(result);
				int strength = levels * ConfigsTB.maxRecursions();
				if(strength > 0)
				{
					BeaconMapping mapping = BeaconMapping.get(level);
					if(mapping != null)
						mapping.forBeacon((BeaconTileEntity) (Object) this).updateLevels(levels);
					((IBeamAcceptor) tile).acceptBeam(new BeaconBeam(worldPosition, worldPosition, pos, start, end, result.getDirection(), strength, strength));
					hasBranchedBeam = true;
				} else
				{
					((IBeamAcceptor) tile).acceptBeam(null);
					hasBranchedBeam = false;
				}
				return blockState.getBeaconColorMultiplier(world, pos, beacon);
			}
		}

		return blockState.getBeaconColorMultiplier(world, pos, beacon);
	}

	@ModifyConstant(
			method = "tick",
			constant = @Constant(
					intValue = 0
			)
	)
	public int tick(int val)
	{
		if(level.getGameTime() % 60L == 0L)
		{
			int i = this.worldPosition.getX();
			int j = this.worldPosition.getY();
			int k = this.worldPosition.getZ();

			this.updateBase(i, j, k);
		}

		return 0;
	}
}