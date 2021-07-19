package org.zeith.comms.c18telebeacons.blocks.redirector;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c18telebeacons.api.tile.IBeamAcceptor;
import org.zeith.comms.c18telebeacons.api.tile.IBeamShooter;
import org.zeith.comms.c18telebeacons.api.tile.TileBaseBeamAcceptor;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.client.render.TESRRedirector;
import org.zeith.comms.c18telebeacons.ConfigsTB;
import org.zeith.comms.c18telebeacons.utils.BlockPosUtils;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.TileRenderer;
import org.zeith.hammerlib.api.forge.TileAPI;

@SimplyRegister
public class TileRedirector
		extends TileBaseBeamAcceptor
		implements IBeamShooter
{
	@TileRenderer(TESRRedirector.class)
	@RegistryName("redirector")
	public static final TileEntityType<TileRedirector> REDIRECTOR = TileAPI.createType(TileRedirector.class, BlockRedirector.REDIRECTOR);

	public TileRedirector()
	{
		super(REDIRECTOR);
	}

	@Override
	public void update()
	{
		super.update();

		if(isOnServer() && _beam != null && _beam.strength > 0 && (atTickRate(40) || beamDirty))
		{
			beamDirty = false;

			BlockState state = this.level.getBlockState(this.worldPosition);

			int rayLength = ConfigsTB.maxRayLength();

			for(Direction dir : Direction.values())
			{
				if(BlockRedirector.isSideOpen(state, dir) && dir != _beam.dir)
				{
					Vector3d start = BlockPosUtils.shootVec(worldPosition, dir);

					for(int shift = 1; shift < rayLength; ++shift)
					{
						BlockPos thePos = worldPosition.relative(dir, shift);
						BlockState theState = level.getBlockState(thePos);
						TileEntity tile = level.getBlockEntity(thePos);
						if(tile instanceof IBeamAcceptor)
						{
							Vector3d end = start.add(Vector3d.atLowerCornerOf(dir.getNormal()).scale(shift));

							BlockRayTraceResult result = BlockPosUtils.clipOneBlock(level, thePos, start, end);
							if(result.getType() == RayTraceResult.Type.MISS)
								continue;

							if(result.getType() == RayTraceResult.Type.BLOCK)
							{
								end = ((IBeamAcceptor) tile).handleEndPositioning(result);
								((IBeamAcceptor) tile).acceptBeam(new BeaconBeam(_beam.beaconPos, worldPosition, thePos, start, end, result.getDirection(), _beam.strength - 1, _beam.maxStrength, _beam.endColor));
								break;
							}
						} else if(theState.isSolidRender(level, thePos) && theState.getBlock() != Blocks.BEDROCK)
						{
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void acceptBeam(BeaconBeam beam)
	{
		if(this.level != null && BlockRedirector.isSideOpen(this.level.getBlockState(this.worldPosition), beam != null ? beam.dir : null))
			super.acceptBeam(beam);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getViewDistance()
	{
		return 256.0D;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public boolean didShoot(BeaconBeam beam)
	{
		return IBeamShooter.super.didShoot(beam)
				&& this._beam != null
				&& this._beam.isSameBeacon(beam)
				&& BlockRedirector.isSideOpen(level.getBlockState(worldPosition), beam.dir.getOpposite())
				&& this._beam.strength > 0;
	}
}