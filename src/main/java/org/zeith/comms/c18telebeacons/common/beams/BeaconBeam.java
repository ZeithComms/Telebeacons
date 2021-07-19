package org.zeith.comms.c18telebeacons.common.beams;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.zeith.comms.c18telebeacons.api.tile.IBeamAcceptor;
import org.zeith.comms.c18telebeacons.api.tile.IBeamShooter;
import org.zeith.comms.c18telebeacons.api.tile.IBranchedBeacon;
import org.zeith.comms.c18telebeacons.common.mapping.BeaconMapping;
import org.zeith.comms.c18telebeacons.utils.BlockPosUtils;
import org.zeith.comms.c18telebeacons.utils.IRepeatableAction;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializationHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BeaconBeam
		implements IAutoNBTSerializable
{
	@NBTSerializable("beacon_pos")
	public BlockPos beaconPos;

	@NBTSerializable("start_pos")
	public BlockPos startPos;

	@NBTSerializable("end_pos")
	public BlockPos endPos;

	@NBTSerializable
	public Vector3d start, end, direction;

	@NBTSerializable
	public double length;

	@NBTSerializable
	public int strength;

	@NBTSerializable("max_strength")
	public int maxStrength;

	@NBTSerializable("base_color")
	public float[] baseColor;

	@NBTSerializable("end_color")
	public float[] endColor;

	@NBTSerializable
	public Direction dir;

	@NBTSerializable
	private BeaconBeamSegment[] segments;

	public BeaconBeam(BlockPos beaconPos, BlockPos startPos, BlockPos endPos, Vector3d start, Vector3d end, Direction dir, int strength, int maxStrength)
	{
		this.beaconPos = beaconPos;
		this.startPos = startPos;
		this.endPos = endPos;
		this.start = start;
		this.end = end;
		this.dir = dir;
		this.direction = end.subtract(start).normalize();
		this.length = start.distanceTo(end);
		this.strength = strength;
		this.maxStrength = maxStrength;
	}

	public BeaconBeam(BlockPos beaconPos, BlockPos startPos, BlockPos endPos, Vector3d start, Vector3d end, Direction dir, int strength, int maxStrength, float[] baseColor)
	{
		this.beaconPos = beaconPos;
		this.startPos = startPos;
		this.endPos = endPos;
		this.start = start;
		this.end = end;
		this.dir = dir;
		this.direction = end.subtract(start).normalize();
		this.length = start.distanceTo(end);
		this.strength = strength;
		this.maxStrength = maxStrength;
		this.baseColor = baseColor;
	}

	public BeaconBeam(CompoundNBT nbt)
	{
		deserializeNBT(nbt);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		BeaconBeam that = (BeaconBeam) o;
		return Double.compare(that.length, length) == 0 && strength == that.strength && maxStrength == that.maxStrength && Objects.equals(beaconPos, that.beaconPos) && Objects.equals(startPos, that.startPos) && Objects.equals(endPos, that.endPos) && Objects.equals(start, that.start) && Objects.equals(end, that.end) && Objects.equals(direction, that.direction) && Arrays.equals(baseColor, that.baseColor) && Arrays.equals(endColor, that.endColor) && dir == that.dir;
	}

	@Override
	public int hashCode()
	{
		int result = Objects.hash(beaconPos, startPos, endPos, start, end, direction, length, strength, maxStrength, dir);
		result = 31 * result + Arrays.hashCode(baseColor);
		result = 31 * result + Arrays.hashCode(endColor);
		return result;
	}

	public void refreshSegments(World world)
	{
		AtomicReference<BeamInfo> inf = new AtomicReference<>();

		LinkedList<BeaconBeamSegment> segments = new LinkedList<>();

		BlockPosUtils.traverseBlocks(start, end, pos ->
		{
			if(pos.equals(startPos)) return IRepeatableAction.Do.CONTINUE;

			if(pos.equals(endPos))
			{
				BeamInfo b = inf.get();
				if(b != null && !segments.isEmpty())
				{
					BeaconBeamSegment s = segments.getLast();
					BeaconBeamSegment bs = s.mix(this, b.pos.distanceTo(end), b.pos, b.colors);
					if(bs != null) segments.add(bs);
				}

				if(segments.isEmpty())
					segments.add(new BeaconBeamSegment(this, start, start.distanceTo(end), baseColor));

				return IRepeatableAction.Do.RETURN;
			}

			if(world.isLoaded(pos) && !world.isEmptyBlock(pos))
			{
				BlockRayTraceResult rtr = BlockPosUtils.clipOneBlock(world, pos, start, end);

				if(rtr == null) // we missed the damn block
					return IRepeatableAction.Do.CONTINUE;

				BlockState state = world.getBlockState(pos);

				if(world.getBlockEntity(pos) instanceof IBeamAcceptor)
				{
					segments.clear();
					return IRepeatableAction.Do.RETURN;
				}

				float[] colorMult = state.getBeaconColorMultiplier(world, pos, startPos);

				if(colorMult == null)
				{
					if(state.isSolidRender(world, pos) && state.getBlock() != Blocks.BEDROCK)
					{
						segments.clear();
						return IRepeatableAction.Do.RETURN;
					} else
						return IRepeatableAction.Do.CONTINUE;
				}

				Vector3d loc = rtr.getLocation();
				BeamInfo b = inf.getAndSet(new BeamInfo(loc, colorMult));

				if(segments.isEmpty())
				{
					segments.add(new BeaconBeamSegment(this, start, start.distanceTo(loc), this.baseColor));
				} else if(b != null)
				{
					BeaconBeamSegment s = segments.getLast();
					BeaconBeamSegment bs = s.mix(this, b.pos.distanceTo(loc), b.pos, b.colors);
					if(bs != null) segments.addLast(bs);
				}
			}

			return IRepeatableAction.Do.CONTINUE;
		});

		if(segments.isEmpty())
		{
			this.segments = null;
			this.endColor = null;
		} else
		{
			this.segments = segments.toArray(new BeaconBeamSegment[0]);
			this.endColor = segments.getLast().color;
		}
	}

	public int getSegmentCount()
	{
		return segments != null ? segments.length : 0;
	}

	public BeaconBeamSegment getSegment(int i)
	{
		return segments[i];
	}

	public boolean isValid()
	{
		return segments != null;
	}

	public boolean isSourceValid(World world)
	{
		if(startPos == null || beaconPos == null || endPos == null) return false;

		TileEntity te;
		return !world.isLoaded(startPos) || ((te = world.getBlockEntity(startPos)) instanceof IBeamShooter && ((IBeamShooter) te).didShoot(this)) || (startPos.equals(beaconPos) && te instanceof IBranchedBeacon);
	}

	public boolean isSameBeacon(BeaconBeam beam)
	{
		return beam != null && beam.beaconPos.equals(beaconPos);
	}

	public float getStrengthF()
	{
		return strength / (float) maxStrength;
	}

	public BeaconMapping.ColorMapping getNetwork(TileEntity node)
	{
		return BeaconMapping.get(node.getLevel()).forBeacon(beaconPos);
	}

	private static class BeamInfo
	{
		Vector3d pos;
		float[] colors;

		public BeamInfo(Vector3d pos, float[] colors)
		{
			this.pos = pos;
			this.colors = colors;
		}
	}

	public static class BeaconBeamSegment
	{
		static final float[] ONE = new float[]{
				1,
				1,
				1
		};

		@NBTSerializable
		public Vector3d start, offset;

		@NBTSerializable
		public double length;

		@NBTSerializable
		public float[] color;

		public BeaconBeamSegment(BeaconBeam beam, Vector3d start, double length, float[] color)
		{
			this.start = start;
			this.length = length;
			this.color = color;
			this.offset = start.subtract(Vector3d.atLowerCornerOf(beam.endPos));
		}

		public BeaconBeamSegment(CompoundNBT nbt)
		{
			NBTSerializationHelper.deserialize(this, nbt);
		}

		public CompoundNBT serializeNBT()
		{
			return NBTSerializationHelper.serialize(this);
		}

		public float[] getColor()
		{
			return color == null ? ONE : color;
		}

		public BeaconBeamSegment mix(BeaconBeam beam, double length, Vector3d start, float[] colorMult)
		{
			if(colorMult == null)
				return null;

			if(Arrays.equals(colorMult, getColor()))
			{
				this.length += length;
				return null;
			}

			float[] dstColor = color == null ? colorMult : new float[]{
					(color[0] + colorMult[0]) / 2,
					(color[1] + colorMult[1]) / 2,
					(color[2] + colorMult[2]) / 2
			};

			return new BeaconBeamSegment(beam, start, length, dstColor);
		}
	}
}