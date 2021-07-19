package org.zeith.comms.c18telebeacons.common.mapping;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.zeith.comms.c18telebeacons.ConfigsTB;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.hammerlib.util.colors.ColorHelper;
import org.zeith.hammerlib.util.mcf.BlockPosList;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class BeaconMapping
		implements INBTSerializable<CompoundNBT>
{
	public final Long2ObjectMap<ColorMapping> beaconMap = new Long2ObjectOpenHashMap<>();

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		beaconMap.forEach((l, m) ->
		{
			nbt.put(Long.toString(l, 16), m.serializeNBT());
		});

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		beaconMap.clear();

		nbt.getAllKeys().forEach(s ->
		{
			try
			{
				long l = Long.parseLong(s, 16);
				ColorMapping cm = new ColorMapping(l);
				cm.deserializeNBT(nbt.getCompound(s));
				beaconMap.put(l, cm);
			} catch(NumberFormatException nfe)
			{
				Telebeacons.LOG.error("Why did you have to go and ruin the beacon mapping??? Completely ignoring beacon pos {}", s);
			}
		});
	}

	public static BeaconMapping get(@Nullable World world)
	{
		return world != null ? world.getCapability(CapabilityBeaconMaps.ENERGY).resolve().orElse(null) : null;
	}

	public boolean putTransceiver(BlockPos beacon, BlockPos transceiver, float[] channel)
	{
		return beaconMap.computeIfAbsent(beacon.asLong(), ColorMapping::new).add(transceiver, channel);
	}

	public void removeTransceiver(BlockPos beacon, BlockPos transceiver)
	{
		ColorMapping mapping = beaconMap.get(beacon.asLong());
		if(mapping != null) mapping.remove(transceiver);
	}

	public ColorMapping forBeacon(BeaconTileEntity beacon)
	{
		return beaconMap.computeIfAbsent(beacon.getBlockPos().asLong(), ColorMapping::new);
	}

	public ColorMapping forBeacon(BlockPos beaconPos)
	{
		return beaconMap.computeIfAbsent(beaconPos.asLong(), ColorMapping::new);
	}

	public void validate(ServerWorld world)
	{
		LongIterator itr = beaconMap.keySet().iterator();
		while(itr.hasNext())
		{
			BlockPos pos = BlockPos.of(itr.nextLong());
			if(world.isLoaded(pos) && !(world.getBlockEntity(pos) instanceof BeaconTileEntity))
				itr.remove();
		}
	}

	public static class WorldBasedMapping
			extends BeaconMapping
	{
		public final World level;

		public WorldBasedMapping(World level)
		{
			this.level = level;
		}
	}

	public static class ColorMapping
			implements INBTSerializable<CompoundNBT>
	{
		public final BlockPos beacon;

		public int maxNodes;

		public final Int2ObjectArrayMap<BlockPosList> colorMap = new Int2ObjectArrayMap<>();
		public final BlockPosList nonColored = new BlockPosList();

		public ColorMapping(long beacon)
		{
			this(BlockPos.of(beacon));
		}

		public ColorMapping(BlockPos beacon)
		{
			this.beacon = beacon;
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			{
				CompoundNBT tag = new CompoundNBT();
				colorMap.forEach((color, list) -> tag.put(Integer.toString(color, 16), list.serializeNBT()));
				nbt.put("ColorMap", tag);
			}
			nbt.put("Default", nonColored.serializeNBT());
			nbt.putInt("MaxNodes", maxNodes);
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			colorMap.clear();
			nonColored.clear();

			maxNodes = nbt.getInt("MaxNodes");

			nonColored.deserializeNBT(nbt.getList("Default", Constants.NBT.TAG_LONG));

			nbt.getCompound("ColorMap").getAllKeys().forEach(s ->
			{
				try
				{
					int color = Integer.parseInt(s, 16);
					colorMap.put(color, new BlockPosList(nbt.getList(s, Constants.NBT.TAG_LONG)));
				} catch(NumberFormatException e)
				{
					Telebeacons.LOG.error("Why did you have to go and ruin the beacon mapping??? Completely ignoring color {}", s);
				}
			});
		}

		public int getNodes()
		{
			int total = nonColored.size();

			ObjectIterator<BlockPosList> iterator = colorMap.values().iterator();
			while(iterator.hasNext())
			{
				BlockPosList list = iterator.next();
				total += list.size();
				if(list.isEmpty()) iterator.remove();
			}

			return total;
		}

		public boolean add(BlockPos transceiver, float[] color)
		{
			remove(transceiver);

			if(getNodes() >= maxNodes) // we don't want to overflow if the network can handle only maxNodes
				return false;

			if(color == null)
			{
				if(!nonColored.contains(transceiver))
					nonColored.add(transceiver);
			} else
			{
				int id = packColor(color);
				BlockPosList list = colorMap.computeIfAbsent(id, i -> new BlockPosList());
				if(!list.contains(transceiver)) list.add(transceiver);
			}

			return true;
		}

		public void remove(BlockPos transceiver)
		{
			nonColored.remove(transceiver);

			ObjectIterator<BlockPosList> iterator = colorMap.values().iterator();
			while(iterator.hasNext())
			{
				BlockPosList list = iterator.next();
				list.remove(transceiver);
				if(list.isEmpty())
					iterator.remove();
			}
		}

		public void updateLevels(int levels)
		{
			maxNodes = 0;
			for(int i = 0; i < levels; ++i)
				maxNodes += ConfigsTB.additionalNodes(i);
		}

		public BlockPos nextNode(float[] channel, BlockPos pos)
		{
			if(channel != null)
			{
				int id = packColor(channel);
				BlockPosList list = colorMap.computeIfAbsent(id, i -> new BlockPosList());
				int idx = list.indexOf(pos);
				if(idx >= 0 && list.size() > 1)
				{
					int nextIdx = (idx + 1) % list.size();
					return list.get(nextIdx);
				} else if(!nonColored.isEmpty())
					return nonColored.get(ThreadLocalRandom.current().nextInt(nonColored.size()));
			} else
			{
				int idx = nonColored.indexOf(pos);
				if(idx >= 0 && nonColored.size() > 1)
				{
					int nextIdx = (idx + 1) % nonColored.size();
					return nonColored.get(nextIdx);
				} else
				{
					IntArrayList list = new IntArrayList(colorMap.keySet());
					while(!list.isEmpty())
					{
						int color = list.removeInt(ThreadLocalRandom.current().nextInt(list.size()));
						BlockPosList subList = colorMap.get(color);
						if(!subList.isEmpty())
							return subList.get(ThreadLocalRandom.current().nextInt(subList.size()));
					}
				}
			}

			return pos;
		}
	}

	public static int packColor(float[] color)
	{
		return ColorHelper.packRGB(color[0], color[1], color[2]);
	}
}