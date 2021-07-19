package org.zeith.comms.c18telebeacons.api.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;

public interface IBeamAcceptor
{
	void acceptBeam(BeaconBeam beam);

	default Vector3d handleEndPositioning(BlockRayTraceResult hitRay)
	{
		return Vector3d.atCenterOf(((TileEntity) this).getBlockPos());
	}
}