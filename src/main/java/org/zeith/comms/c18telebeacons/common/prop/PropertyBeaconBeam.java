package org.zeith.comms.c18telebeacons.common.prop;

import net.minecraft.network.PacketBuffer;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.hammerlib.net.properties.IProperty;
import org.zeith.hammerlib.net.properties.PropertyDispatcher;
import org.zeith.hammerlib.util.java.DirectStorage;

import java.util.Objects;

public class PropertyBeaconBeam
		implements IProperty<BeaconBeam>
{
	final DirectStorage<BeaconBeam> value;

	public PropertyBeaconBeam(DirectStorage<BeaconBeam> value)
	{
		this.value = value;
	}

	public PropertyBeaconBeam()
	{
		this(DirectStorage.allocate(null));
	}

	@Override
	public Class<BeaconBeam> getType()
	{
		return BeaconBeam.class;
	}

	@Override
	public BeaconBeam set(BeaconBeam value)
	{
		BeaconBeam pv = this.value.get();
		if(!Objects.equals(pv, value))
		{
			this.value.set(value);
			markChanged(true);
		}
		return pv;
	}

	boolean changed;

	@Override
	public void markChanged(boolean changed)
	{
		this.changed = changed;
		if(changed) notifyDispatcherOfChange();
	}

	@Override
	public boolean hasChanged()
	{
		return changed;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		BeaconBeam value = this.value.get();
		buf.writeBoolean(value != null);
		if(value != null)
			buf.writeNbt(value.serializeNBT());
	}

	@Override
	public void read(PacketBuffer buf)
	{
		value.set(buf.readBoolean() ? new BeaconBeam(buf.readNbt()) : null);
	}

	@Override
	public BeaconBeam get()
	{
		return value.get();
	}

	PropertyDispatcher dispatcher;

	@Override
	public PropertyDispatcher getDispatcher()
	{
		return dispatcher;
	}

	@Override
	public void setDispatcher(PropertyDispatcher dispatcher)
	{
		this.dispatcher = dispatcher;
	}
}