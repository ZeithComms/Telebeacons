package org.zeith.comms.c18telebeacons.blocks.transceiver;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.api.tile.TileBaseBeamAcceptor;
import org.zeith.comms.c18telebeacons.client.Flash;
import org.zeith.comms.c18telebeacons.client.particles.WhooshParticleData;
import org.zeith.comms.c18telebeacons.client.render.TESRTransceiver;
import org.zeith.comms.c18telebeacons.common.beams.BeaconBeam;
import org.zeith.comms.c18telebeacons.common.mapping.BeaconMapping;
import org.zeith.comms.c18telebeacons.net.PacketBeamFlash;
import org.zeith.comms.c18telebeacons.net.PacketPlayPositionlessSound;
import org.zeith.comms.c18telebeacons.net.PacketStartFlash;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.TileRenderer;
import org.zeith.hammerlib.api.forge.TileAPI;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.physics.FrictionRotator;

import java.util.*;

@SimplyRegister
public class TileTransceiver
		extends TileBaseBeamAcceptor
{
	public static final String TB_CD = Telebeacons.MOD_ID + ":cooldown";
	public static final String TB_CD_OWNER = Telebeacons.MOD_ID + ":cooldown_owner";

	@TileRenderer(TESRTransceiver.class)
	@RegistryName("transceiver")
	public static final TileEntityType<TileTransceiver> TRANSCEIVER = TileAPI.createType(TileTransceiver.class, BlockTransceiver.TRANSCEIVER);

	@NBTSerializable
	public final FrictionRotator spinny = new FrictionRotator();

	@NBTSerializable("last_check_color")
	protected float[] prevNetColor;

	@NBTSerializable("beacon_pos")
	protected BlockPos netPos;

	public int prevOpen, nowOpen;

	public int prevFlash, nowFlash;

	public TileTransceiver()
	{
		super(TRANSCEIVER);
		spinny.friction = 0.25F;
	}

	@Override
	public void update()
	{
		super.update();

		prevOpen = nowOpen;
		prevFlash = nowFlash;

		if(isOnServer())
		{
			float[] netColor = getNetworkColor();
			if(!Arrays.equals(prevNetColor, netColor))
			{
				prevNetColor = netColor;
				updateBeaconBeam(_beam, _beam);
			}

			BlockPos beaconPos = _beam != null ? _beam.beaconPos : null;
			if(!Objects.equals(netPos, beaconPos))
			{
				BeaconMapping mapping = BeaconMapping.get(level);

				if(mapping != null)
				{
					if(netPos != null) mapping.removeTransceiver(netPos, worldPosition);
					if(beaconPos != null && !mapping.putTransceiver(beaconPos, worldPosition, netColor))
					{
						beam.set(null);
					}
				}

				netPos = beaconPos;
			}
		}

		if(nowFlash > 0)
		{
			++nowFlash;
			if(nowFlash == 48)
			{
				prevFlash = 0;
				nowFlash = 0;
			}
		}

		spinny.update();

		boolean hasBeam = _beam != null && _beam.isSourceValid(level);
		boolean enable = hasBeam && !(level.getBestNeighborSignal(worldPosition) <= 0 || getBlockState().getValue(BlockStateProperties.INVERTED));

		AxisAlignedBB aabb = BlockTransceiver.getTeleportationBounds(level, worldPosition);
		List<Entity> targetEntities = aabb != null ? level.getEntities((Entity) null, aabb, e -> e.getPersistentData().getInt(TB_CD) <= 0) : Collections.emptyList();

		if(enable)
		{
			spinny.speedupTo(3F, 0.25F);

			if(isOnClient())
			{
				Random rand = level.getRandom();
				if(level != null)
				{
					double x = MathHelper.lerp(rand.nextFloat(), aabb.minX, aabb.maxX),
							y = MathHelper.lerp(rand.nextFloat(), aabb.minY, aabb.maxY),
							z = MathHelper.lerp(rand.nextFloat(), aabb.minZ, aabb.maxZ);
					level.addParticle(WhooshParticleData.create(prevNetColor), x, y, z, 0, 0, 0);
				}
			}

			if(this.level.getGameTime() % 80L == 0L)
			{
				this.playSound(BlockTransceiver.TRANSCEIVER_AMBIENCE);
			}
		}

		if(enable && !targetEntities.isEmpty())
		{
			spinny.speedupTo(16F, 2F);
			if(nowOpen < 10) ++nowOpen;
			else if(isOnServer()) ++nowOpen;
		} else if(nowOpen > 0) --nowOpen;

		if(isOnServer() && _beam != null && level instanceof ServerWorld)
		{
			ServerWorld sv = (ServerWorld) level;

			BeaconMapping.ColorMapping network = _beam.getNetwork(this);
			BlockPos dst = network.nextNode(prevNetColor, worldPosition);
			if(dst != null)
			{
				if(aabb != null)
				{
					if(nowOpen == 14)
					{
						SPlaySoundEffectPacket pkt = new SPlaySoundEffectPacket(Flash.FLASH, SoundCategory.BLOCKS, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1F, 1F);

						for(ServerPlayerEntity spe : level.getEntitiesOfClass(ServerPlayerEntity.class, aabb.inflate(32)))
							if(!targetEntities.contains(spe))
							{
								Network.sendTo(new PacketStartFlash(worldPosition), spe);
								spe.connection.send(pkt);
							}

						AxisAlignedBB aabb2 = BlockTransceiver.getTeleportationBounds(level, dst);
						pkt = new SPlaySoundEffectPacket(Flash.FLASH, SoundCategory.BLOCKS, dst.getX(), dst.getY(), dst.getZ(), 1F, 1F);

						for(ServerPlayerEntity spe : level.getEntitiesOfClass(ServerPlayerEntity.class, aabb2.inflate(32)))
							if(!targetEntities.contains(spe))
							{
								Network.sendTo(new PacketStartFlash(dst), spe);
								spe.connection.send(pkt);
							}

						targetEntities.forEach(Flash::tpFlash);

						Network.sendToAll(new PacketBeamFlash(_beam.beaconPos));
					}

					if(nowOpen == 20)
					{
						Chunk loadedChunk = sv.getChunkAt(dst);
						BlockState state = loadedChunk.getBlockState(dst);
						if(state.getBlock() == BlockTransceiver.TRANSCEIVER)
						{
							float ourFloor = BlockTransceiver.getTeleportationFloor(level, worldPosition);
							float dstFloor = BlockTransceiver.getTeleportationFloor(level, dst);

							Vector3d center = aabb.getCenter();
							SPlaySoundEffectPacket pkt = new SPlaySoundEffectPacket(BlockTransceiver.WHOOSH, SoundCategory.BLOCKS, center.x, center.y, center.z, 1F, 1F);

							for(ServerPlayerEntity spe : level.getEntitiesOfClass(ServerPlayerEntity.class, aabb.inflate(32)))
								if(!targetEntities.contains(spe))
									spe.connection.send(pkt);

							targetEntities.forEach(e ->
							{
								Vector3d relative = BlockTransceiver.getRelative(e, worldPosition, ourFloor);
								Vector3d newPos = BlockTransceiver.relative2new(e, dst, dstFloor, relative);

								if(e instanceof ServerPlayerEntity)
								{
									((ServerPlayerEntity) e).connection.teleport(newPos.x, newPos.y, newPos.z, e.yRot, e.xRot);
									Network.sendTo(new PacketPlayPositionlessSound(BlockTransceiver.WHOOSH, SoundCategory.BLOCKS, 1F), (ServerPlayerEntity) e);
								} else
									e.teleportTo(newPos.x, newPos.y, newPos.z);

								e.getPersistentData().putInt(TB_CD, 30);
								e.getPersistentData().putLong(TB_CD_OWNER, dst.asLong());
							});

							aabb = BlockTransceiver.getTeleportationBounds(level, dst);
							center = aabb.getCenter();
							pkt = new SPlaySoundEffectPacket(BlockTransceiver.WHOOSH, SoundCategory.BLOCKS, center.x, center.y, center.z, 1F, 1F);

							for(ServerPlayerEntity spe : level.getEntitiesOfClass(ServerPlayerEntity.class, aabb.inflate(32)))
								if(!targetEntities.contains(spe))
									spe.connection.send(pkt);
								else
									spe.connection.send(new SPlaySoundEffectPacket(BlockTransceiver.TRANSCEIVER_AMBIENCE, SoundCategory.BLOCKS, center.x, center.y, center.z, 2F, 1F));
						}

						nowOpen = 0;
					}
				}
			}
		}
	}

	public void playSound(SoundEvent sound)
	{
		this.level.playSound(null, this.worldPosition, sound, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}

	@Override
	protected boolean updateBeaconBeam(BeaconBeam oldBeam, BeaconBeam newBeam)
	{
		BeaconMapping mapping = BeaconMapping.get(level);

		if(mapping != null)
		{
			if(oldBeam != null)
				mapping.removeTransceiver(oldBeam.beaconPos, worldPosition);

			if(newBeam != null)
			{
				boolean handled = mapping.putTransceiver(newBeam.beaconPos, worldPosition, newBeam.endColor);
				if(handled) netPos = newBeam.beaconPos;
				else return false;
			}
		}

		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getViewDistance()
	{
		return 256.0D;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public Vector3d handleEndPositioning(BlockRayTraceResult hitRay)
	{
		BlockState state = level.getBlockState(worldPosition);
		if(state.getBlock() == BlockTransceiver.TRANSCEIVER && state.getValue(BlockTransceiver.FACING).getOpposite() == hitRay.getDirection())
			return hitRay.getLocation();
		return Vector3d.atCenterOf(worldPosition);
	}
}