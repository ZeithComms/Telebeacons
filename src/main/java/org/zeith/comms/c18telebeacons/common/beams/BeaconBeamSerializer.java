package org.zeith.comms.c18telebeacons.common.beams;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import org.zeith.hammerlib.api.io.NBTSerializer;
import org.zeith.hammerlib.api.io.serializers.INBTSerializer;

@NBTSerializer(BeaconBeam.class)
public class BeaconBeamSerializer
		implements INBTSerializer<BeaconBeam>
{
	@Override
	public void serialize(CompoundNBT nbt, String key, BeaconBeam value)
	{
		if(value != null) nbt.put(key, value.serializeNBT());
	}

	@Override
	public BeaconBeam deserialize(CompoundNBT nbt, String key)
	{
		return nbt.contains(key, Constants.NBT.TAG_COMPOUND) ? new BeaconBeam(nbt.getCompound(key)) : null;
	}
}
