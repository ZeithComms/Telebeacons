package org.zeith.comms.c18telebeacons.common.beams;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import org.zeith.hammerlib.api.io.NBTSerializer;
import org.zeith.hammerlib.api.io.serializers.INBTSerializer;

import static org.zeith.comms.c18telebeacons.common.beams.BeaconBeam.BeaconBeamSegment;

@NBTSerializer(BeaconBeamSegment.class)
public class BeaconBeamSegmentSerializer
		implements INBTSerializer<BeaconBeamSegment>
{
	@Override
	public void serialize(CompoundNBT nbt, String key, BeaconBeamSegment value)
	{
		if(value != null) nbt.put(key, value.serializeNBT());
	}

	@Override
	public BeaconBeamSegment deserialize(CompoundNBT nbt, String key)
	{
		return nbt.contains(key, Constants.NBT.TAG_COMPOUND) ? new BeaconBeamSegment(nbt.getCompound(key)) : null;
	}
}
