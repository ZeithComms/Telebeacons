package org.zeith.comms.c18telebeacons.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BlockPosUtils
{
	public static BlockPos setY(BlockPos pos, int y)
	{
		if(pos instanceof BlockPos.Mutable)
			return setY((BlockPos.Mutable) pos, y);
		return new BlockPos(pos.getX(), y, pos.getZ());
	}

	public static BlockRayTraceResult clipOneBlock(World world, BlockPos pos, Vector3d start, Vector3d end)
	{
		BlockState blockstate = world.getBlockState(pos);
		VoxelShape voxelshape = RayTraceContext.BlockMode.COLLIDER.get(blockstate, world, pos, ISelectionContext.empty());
		return world.clipWithInteractionOverride(start, end, pos, voxelshape, blockstate);
	}

	public static BlockPos.Mutable setY(BlockPos.Mutable pos, int y)
	{
		return pos.set(pos.getX(), y, pos.getZ());
	}

	public static Vector3d shootVec(BlockPos center, Direction towards)
	{
		return new Vector3d(center.getX() + 0.5 + towards.getStepX() * 0.501, center.getY() + 0.5 + towards.getStepY() * 0.501, center.getZ() + 0.5 + towards.getStepZ() * 0.501);
	}

	public static void traverseBlocks(Vector3d from, Vector3d to, IRepeatableAction<BlockPos> traverser)
	{
		if(!from.equals(to))
		{
			double t2fX = MathHelper.lerp(-1.0E-7D, to.x, from.x);
			double t2FY = MathHelper.lerp(-1.0E-7D, to.y, from.y);
			double t2FZ = MathHelper.lerp(-1.0E-7D, to.z, from.z);

			double f2tX = MathHelper.lerp(-1.0E-7D, from.x, to.x);
			double f2tY = MathHelper.lerp(-1.0E-7D, from.y, to.y);
			double f2tZ = MathHelper.lerp(-1.0E-7D, from.z, to.z);

			int x = MathHelper.floor(f2tX);
			int y = MathHelper.floor(f2tY);
			int z = MathHelper.floor(f2tZ);

			BlockPos.Mutable tmp = new BlockPos.Mutable(x, y, z);

			if(!traverser.accept(tmp).canContinue())
				return;

			double dX = t2fX - f2tX;
			double dY = t2FY - f2tY;
			double dZ = t2FZ - f2tZ;

			int sX = MathHelper.sign(dX);
			int sY = MathHelper.sign(dY);
			int sZ = MathHelper.sign(dZ);

			double shiftX = sX == 0 ? Double.MAX_VALUE : (double) sX / dX;
			double shiftY = sY == 0 ? Double.MAX_VALUE : (double) sY / dY;
			double shiftZ = sZ == 0 ? Double.MAX_VALUE : (double) sZ / dZ;

			double fracX = shiftX * (sX > 0 ? 1.0D - MathHelper.frac(f2tX) : MathHelper.frac(f2tX));
			double fracY = shiftY * (sY > 0 ? 1.0D - MathHelper.frac(f2tY) : MathHelper.frac(f2tY));
			double fracZ = shiftZ * (sZ > 0 ? 1.0D - MathHelper.frac(f2tZ) : MathHelper.frac(f2tZ));

			while(fracX <= 1.0D || fracY <= 1.0D || fracZ <= 1.0D)
			{
				if(fracX < fracY)
				{
					if(fracX < fracZ)
					{
						x += sX;
						fracX += shiftX;
					} else
					{
						z += sZ;
						fracZ += shiftZ;
					}
				} else if(fracY < fracZ)
				{
					y += sY;
					fracY += shiftY;
				} else
				{
					z += sZ;
					fracZ += shiftZ;
				}

				if(!traverser.accept(tmp.set(x, y, z)).canContinue())
					return;
			}
		}
	}
}