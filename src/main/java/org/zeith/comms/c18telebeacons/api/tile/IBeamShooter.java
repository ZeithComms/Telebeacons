package org.zeith.comms.c18telebeacons.api.tile;

import net.minecraft.tileentity.TileEntity;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;

public interface IBeamShooter
{
	default boolean didShoot(BeaconBeam beam)
	{
		TileEntity tile = (TileEntity) this;
		return beam.startPos.equals(tile.getBlockPos());
	}
}