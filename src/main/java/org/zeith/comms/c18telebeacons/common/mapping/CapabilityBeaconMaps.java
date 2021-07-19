package org.zeith.comms.c18telebeacons.common.mapping;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.hammerlib.annotations.Setup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityBeaconMaps
{
	@CapabilityInject(BeaconMapping.class)
	public static Capability<BeaconMapping> ENERGY = null;

	@Setup
	public static void register()
	{
		Telebeacons.LOG.info("Registered beacon maps capability!");

		MinecraftForge.EVENT_BUS.addGenericListener(World.class, CapabilityBeaconMaps::attach);
		MinecraftForge.EVENT_BUS.addListener(CapabilityBeaconMaps::updateWorld);

		CapabilityManager.INSTANCE.register(BeaconMapping.class, new Capability.IStorage<BeaconMapping>()
				{
					@Override
					public INBT writeNBT(Capability<BeaconMapping> capability, BeaconMapping instance, Direction side)
					{
						return instance.serializeNBT();
					}

					@Override
					public void readNBT(Capability<BeaconMapping> capability, BeaconMapping instance, Direction side, INBT nbt)
					{
						if(instance == null)
							throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
						instance.deserializeNBT((CompoundNBT) nbt);
					}
				},
				BeaconMapping::new);
	}

	private static void attach(AttachCapabilitiesEvent<World> e)
	{
		if(e.getObject() instanceof ServerWorld)
			e.addCapability(new ResourceLocation(Telebeacons.MOD_ID, "beacon_mapping"), new BeaconMappingProvider(e.getObject()));
	}

	private static void updateWorld(TickEvent.WorldTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START && e.side == LogicalSide.SERVER && e.world instanceof ServerWorld && e.world.getGameTime() % 100L == 0L)
		{
			ServerWorld world = (ServerWorld) e.world;

			BeaconMapping mapping = BeaconMapping.get(world);
			if(mapping != null) mapping.validate(world);
		}
	}

	public static class BeaconMappingProvider
			implements ICapabilityProvider, INBTSerializable<CompoundNBT>
	{
		final BeaconMapping mapping;
		final LazyOptional<BeaconMapping> mappingLazyOptional;

		public BeaconMappingProvider(World world)
		{
			this.mapping = new BeaconMapping.WorldBasedMapping(world);
			this.mappingLazyOptional = LazyOptional.of(() -> mapping);
		}

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
		{
			return ENERGY.orEmpty(cap, mappingLazyOptional);
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			return mapping.serializeNBT();
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			mapping.deserializeNBT(nbt);
		}
	}
}