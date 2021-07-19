package org.zeith.comms.c18telebeacons.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comms.c18telebeacons.ConfigsTB;
import org.zeith.comms.c18telebeacons.api.tile.IBeamAcceptor;
import org.zeith.comms.c18telebeacons.api.tile.IBranchedBeacon;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.common.mapping.BeaconMapping;
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

	private boolean hasBranchedBeam;
	private int branchedLevels;

	public boolean IBranchedBeacon$hasBranchedBeam()
	{
		return hasBranchedBeam;
	}

	public int IBranchedBeacon$getBranchedBeaconLevel()
	{
		return branchedLevels;
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;getHeight(Lnet/minecraft/world/gen/Heightmap$Type;II)I"
			)
	)
	public void tick(CallbackInfo ci)
	{
		if(level.getGameTime() % 60L == 0L)
		{
			BlockPos beacon = worldPosition;
			BlockPos pos = beacon.above();

			int x = beacon.getX();
			int y = beacon.getY();
			int z = beacon.getZ();
			this.updateBase(x, y, z);
			branchedLevels = levels;

			int l = this.level.getHeight(Heightmap.Type.WORLD_SURFACE, x, z);

			while(pos.getY() <= l)
			{
				BlockState blockState = this.level.getBlockState(pos);
				Block block = blockState.getBlock();
				float[] afloat = blockState.getBeaconColorMultiplier(this.level, pos, getBlockPos());
				if(afloat == null)
				{
					TileEntity tile = level.getBlockEntity(pos);
					if(tile instanceof IBeamAcceptor)
					{
						Vector3d start = new Vector3d(beacon.getX() + 0.5, beacon.getY() + 0.75, beacon.getZ() + 0.5);
						Vector3d end = new Vector3d(beacon.getX() + 0.5, pos.getY() + 0.9999, beacon.getZ() + 0.5);

						BlockRayTraceResult result = BlockPosUtils.clipOneBlock(level, pos, start, end);
						if(result.getType() == RayTraceResult.Type.MISS)
							break;

						if(result.getType() == RayTraceResult.Type.BLOCK)
						{
							end = ((IBeamAcceptor) tile).handleEndPositioning(result);
							int strength = branchedLevels * ConfigsTB.maxRecursions();
							if(strength > 0)
							{
								BeaconMapping mapping = BeaconMapping.get(level);
								if(mapping != null)
									mapping.forBeacon((BeaconTileEntity) (Object) this).updateLevels(branchedLevels);
								((IBeamAcceptor) tile).acceptBeam(new BeaconBeam(beacon, beacon, pos, start, end, result.getDirection(), strength, strength));
								hasBranchedBeam = true;
							} else
							{
								((IBeamAcceptor) tile).acceptBeam(null);
								hasBranchedBeam = false;
							}
							break;
						}
					}

					if(blockState.getLightBlock(this.level, pos) >= 15 && block != Blocks.BEDROCK)
						break;
				}

				pos = pos.above();
			}
		}
	}
}