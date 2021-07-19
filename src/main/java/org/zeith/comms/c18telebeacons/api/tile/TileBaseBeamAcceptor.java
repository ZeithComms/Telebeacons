package org.zeith.comms.c18telebeacons.api.tile;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.Explosion;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.common.prop.PropertyBeaconBeam;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.tiles.TileSyncableTickable;
import org.zeith.hammerlib.util.java.DirectStorage;

import java.util.Objects;

public class TileBaseBeamAcceptor
		extends TileSyncableTickable
		implements IBeamAcceptor
{
	@NBTSerializable("beam")
	public BeaconBeam _beam;

	protected boolean beamDirty;

	public final PropertyBeaconBeam beam = new PropertyBeaconBeam(DirectStorage.create(b -> _beam = b, () -> _beam));

	public TileBaseBeamAcceptor(TileEntityType<?> type)
	{
		super(type);
		this.dispatcher.registerProperty("beam", beam);
	}

	public float[] getNetworkColor()
	{
		return _beam != null ? _beam.endColor : null;
	}

	protected boolean updateBeaconBeam(BeaconBeam oldBeam, BeaconBeam newBeam)
	{
		return true;
	}

	@Override
	public void update()
	{
		if(isOnServer() && _beam != null)
		{
			if(_beam.beaconPos == null || _beam.endPos == null || _beam.startPos == null)
			{
				beam.set(null);
			} else if(beamDirty || atTickRate(10))
			{
				if(!_beam.isSourceValid(level))
					beam.set(null);
				if(_beam != null)
				{
					_beam.refreshSegments(level);
					beam.markChanged(true);
					if(!_beam.isValid())
						beam.set(null);
				}
			}
		}
	}

	@Override
	public void acceptBeam(BeaconBeam beam)
	{
		if(level == null || level.isClientSide) return;

		BeaconBeam _beam = this.beam.get();

		if(beam == null)
		{
			this.beamDirty = true;
			if(updateBeaconBeam(_beam, null))
				this.beam.set(null);
			sync();
		} else if(beam.isSourceValid(level))
		{
			if(_beam != null)
			{
				if(_beam.isSameBeacon(beam))
				{
					if(!Objects.equals(_beam.startPos, beam.startPos))
					{
						level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4, Explosion.Mode.NONE);
						level.destroyBlock(worldPosition, true);
					} else
					{
						_beam.strength = beam.strength;
						_beam.baseColor = beam.baseColor;
						updateBeaconBeam(_beam, _beam);
					}
				} else if(_beam.isSourceValid(level))
				{
					level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4, Explosion.Mode.NONE);
					level.destroyBlock(worldPosition, true);
					return;
				}
			}

			beam.refreshSegments(level);
			if(updateBeaconBeam(_beam, beam))
				this.beam.set(beam);
			this.beamDirty = true;

			sync();
		}
	}
}
