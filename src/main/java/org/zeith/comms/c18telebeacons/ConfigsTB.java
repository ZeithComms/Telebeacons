package org.zeith.comms.c18telebeacons;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigsTB
{
	public static int maxRayLength()
	{
		return COMMON.maxRayLength.get();
	}

	public static int maxRecursions()
	{
		return COMMON.maxRecursions.get();
	}

	public static int additionalNodes(int level)
	{
		switch(level)
		{
			case 0:
				return COMMON.level1.get();
			case 1:
				return COMMON.level2.get();
			case 3:
				return COMMON.level3.get();
			default:
				return COMMON.level4.get();
		}
	}

	public static class Common
	{
		public final ForgeConfigSpec.IntValue maxRayLength;
		public final ForgeConfigSpec.IntValue maxRecursions;
		public final ForgeConfigSpec.IntValue level1, level2, level3, level4;

		Common(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Redirector block configurations")
					.push("redirector");
			{
				maxRayLength = builder
						.comment("Maximal length the redirector will check for other beam acceptors. Setting to a value higher than 256 is not recommended, although is possible, but might cause poor performance.")
						.defineInRange("maxRayLength", 256, 0, 512);

				maxRecursions = builder
						.comment("Maximal number of recursions per beacon level. Higher values allow for more redirectors to be daisy-chained.")
						.defineInRange("maxRecursions", 8, 0, Integer.MAX_VALUE);
			}
			builder.pop();

			builder.comment("Beacon power configurations. Each level adds the given amount to the previous value, by default the maxed out beacon will have a total maximum of 4+8+12+16=40 transceivers attached to the network.")
					.push("beaconPower");
			{
				level1 = builder
						.comment("Level 1 max transceiver connections")
						.defineInRange("lvl1", 4, 0, Integer.MAX_VALUE);

				level2 = builder
						.comment("Level 2 max transceiver connections")
						.defineInRange("lvl2", 8, 0, Integer.MAX_VALUE);

				level3 = builder
						.comment("Level 3 max transceiver connections")
						.defineInRange("lvl3", 12, 0, Integer.MAX_VALUE);

				level4 = builder
						.comment("Level 4 max transceiver connections")
						.defineInRange("lvl4", 16, 0, Integer.MAX_VALUE);
			}
			builder.pop();
		}
	}

	static final ForgeConfigSpec commonSpec;
	public static final ConfigsTB.Common COMMON;

	static
	{
		final Pair<ConfigsTB.Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigsTB.Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}