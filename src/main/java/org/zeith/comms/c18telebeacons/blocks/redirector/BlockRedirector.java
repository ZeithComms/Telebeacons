package org.zeith.comms.c18telebeacons.blocks.redirector;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import org.zeith.api.wrench.IWrenchable;
import org.zeith.comms.c18telebeacons.Telebeacons;
import org.zeith.comms.c18telebeacons.api.block.ITelebeaconsBlock;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.api.fml.IRegisterListener;
import org.zeith.hammerlib.event.listeners.ServerListener;

import javax.annotation.Nullable;

import static net.minecraft.state.properties.BlockStateProperties.*;

@SimplyRegister
public class BlockRedirector
		extends ContainerBlock
		implements ITelebeaconsBlock, IRegisterListener, IWrenchable
{
	@RegistryName("redirector")
	public static final BlockRedirector REDIRECTOR = new BlockRedirector(AbstractBlock.Properties.of(Material.GLASS).harvestLevel(2).harvestTool(ToolType.PICKAXE).strength(3F).requiresCorrectToolForDrops());

	public BlockRedirector(Properties props)
	{
		super(props);

		registerDefaultState(defaultBlockState()
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false)
		);
	}

	@Override
	public void onPostRegistered()
	{
		// Prevent these model states from being loaded in during model loading for blockstates.
		getStateDefinition().getPossibleStates().forEach(Telebeacons.PROXY::excludeModelState);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWERED);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray)
	{
		if(player.getItemInHand(hand).isEmpty() && hand == Hand.MAIN_HAND)
		{
			BooleanProperty prop = DIRECTION_PROPERTIES.get(ray.getDirection());

			TileEntity tile = world.getBlockEntity(pos);

			world.setBlock(pos, state = state.setValue(prop, !state.getValue(prop)), 3);
			world.playSound(player, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1, state.getValue(prop) ? 1.2F : 0.8F);

			tile.clearRemoved();
			world.setBlockEntity(pos, tile);

			if(tile instanceof TileRedirector)
			{
				TileRedirector r = (TileRedirector) tile;
				if(r._beam != null && r._beam.dir == ray.getDirection() && !state.getValue(prop))
					r.beam.set(null);
			}

			ServerListener.syncTileEntity(tile);

			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	public boolean onWrenchUsed(ItemUseContext context)
	{
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		BooleanProperty prop = DIRECTION_PROPERTIES.get(context.getClickedFace());
		world.setBlock(pos, state.setValue(prop, !state.getValue(prop)), 3);
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blk, BlockPos fromPos, boolean marker)
	{
		super.neighborChanged(state, world, pos, blk, fromPos, marker);
		world.setBlock(pos, state.setValue(POWERED, world.getBestNeighborSignal(pos) > 0), 3);
	}

	public static final ImmutableMap<Direction, BooleanProperty> DIRECTION_PROPERTIES = ImmutableMap.<Direction, BooleanProperty> builder()
			.put(Direction.NORTH, NORTH)
			.put(Direction.EAST, EAST)
			.put(Direction.SOUTH, SOUTH)
			.put(Direction.WEST, WEST)
			.put(Direction.UP, UP)
			.put(Direction.DOWN, DOWN)
			.build();

	public static boolean isSideOpen(BlockState state, Direction side)
	{
		if(side == null)
			return true;
		BooleanProperty prop = DIRECTION_PROPERTIES.get(side);
		if(state.getValues().containsKey(prop))
			return state.getValue(prop);
		return false;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader world)
	{
		return new TileRedirector();
	}

	/*
	@Override
	public void makeBranches(World world, BlockPos pos, Direction from, BranchingBeamSegment segment)
	{
		BlockState state = world.getBlockState(pos);
		if(state.getValue(DIRECTION_PROPERTIES.get(from)))
			DIRECTION_PROPERTIES.forEach((dir, prop) ->
			{
				if(state.getValue(prop) && dir != from)
				{
					segment.shootBeaconFromEnd(dir);
				}
			});
	}
	*/
}