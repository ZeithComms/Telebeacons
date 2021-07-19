package org.zeith.comms.c18telebeacons.api.tile;

import net.minecraft.tileentity.BeaconTileEntity;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;

import java.util.Objects;

/**
 * Interface added to the {@link net.minecraft.tileentity.BeaconTileEntity}
 */
public interface IBranchedBeacon
		extends IBeamShooter
{
	boolean hasBranchedBeam();

	int getBranchedBeaconLevel();

	default BeaconTileEntity asBeacon()
	{
		return (BeaconTileEntity) this;
	}

	@Override
	default boolean didShoot(BeaconBeam beam)
	{
		return Objects.equals(beam.beaconPos, asBeacon().getBlockPos());
	}
}