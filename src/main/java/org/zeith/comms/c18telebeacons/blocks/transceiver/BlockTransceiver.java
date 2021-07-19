package org.zeith.comms.c18telebeacons.blocks.transceiver;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.ToolType;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.api.block.ITelebeaconsBlock;
import org.zeith.comms.c18telebeacons.common.mapping.BeaconMapping;
import org.zeith.comms.c18telebeacons.init.BlocksTB;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

import javax.annotation.Nullable;

import static net.minecraft.state.properties.BlockStateProperties.INVERTED;
import static net.minecraft.state.properties.BlockStateProperties.POWERED;

@SimplyRegister
public class BlockTransceiver
		extends ContainerBlock
		implements ITelebeaconsBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.UP, Direction.DOWN);

	@RegistryName("transceiver")
	public static final BlockTransceiver TRANSCEIVER = new BlockTransceiver(AbstractBlock.Properties.of(Material.METAL).lightLevel(s -> 15).strength(3F).harvestLevel(2).harvestTool(ToolType.PICKAXE).noOcclusion().requiresCorrectToolForDrops().isValidSpawn(BlocksTB::never).isRedstoneConductor(BlocksTB::never).emissiveRendering(BlocksTB::always).isSuffocating(BlocksTB::never).isViewBlocking(BlocksTB::never));

	@RegistryName("whoosh")
	public static final SoundEvent WHOOSH = new SoundEvent(new ResourceLocation(Telebeacons.MOD_ID, "whoosh"));

	@RegistryName("transceiver_ambience")
	public static final SoundEvent TRANSCEIVER_AMBIENCE = new SoundEvent(new ResourceLocation(Telebeacons.MOD_ID, "transceiver_ambience"));

	protected BlockTransceiver(Properties props)
	{
		super(props);
		registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(INVERTED, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		return defaultBlockState().setValue(FACING, ctx.getPlayer().xRot > 0 ? Direction.UP : Direction.DOWN);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(POWERED, INVERTED, FACING);
	}

	@Override
	public BlockRenderType getRenderShape(BlockState p_149645_1_)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getVisualShape(BlockState p_230322_1_, IBlockReader p_230322_2_, BlockPos p_230322_3_, ISelectionContext p_230322_4_)
	{
		return VoxelShapes.empty();
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState uhhh, boolean flag)
	{
		TileEntity tile = world.getBlockEntity(pos);
		if(tile instanceof TileTransceiver)
		{
			TileTransceiver tt = (TileTransceiver) tile;
			if(tt.netPos != null)
			{
				BeaconMapping mapping = BeaconMapping.get(world);
				if(mapping != null) mapping.removeTransceiver(tt.netPos, pos);
			}
		}
		super.onRemove(state, world, pos, uhhh, flag);
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader world)
	{
		return new TileTransceiver();
	}

	@Nullable
	public static AxisAlignedBB getTeleportationBounds(World world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);

		if(state.getBlock() == TRANSCEIVER)
		{
			Direction upOrDown = state.getValue(FACING);

			if(upOrDown == Direction.UP)
				return new AxisAlignedBB(pos).inflate(1).move(0, 3, 0);
			else if(upOrDown == Direction.DOWN)
				return new AxisAlignedBB(pos).inflate(1).move(0, -3, 0);
		}

		return null;
	}

	public static float getTeleportationFloor(World world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);

		if(state.getBlock() == TRANSCEIVER)
		{
			Direction upOrDown = state.getValue(FACING);

			if(upOrDown == Direction.UP)
				return pos.getY() + 2;
			else if(upOrDown == Direction.DOWN)
				return pos.getY() - 4;
		}

		return world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
	}

	public static Vector3d getRelative(Entity ent, BlockPos pos, float thisFloor)
	{
		Vector3d center = Vector3d.atCenterOf(pos);
		return new Vector3d(center.x - ent.getX(), ent.getY() - thisFloor, center.z - ent.getZ());
	}

	public static Vector3d relative2new(Entity ent, BlockPos dstPos, float dstFloor, Vector3d relative)
	{
		Vector3d center = Vector3d.atCenterOf(dstPos);
		return new Vector3d(center.x - relative.x, dstFloor + relative.y, center.z - relative.z);
	}
}