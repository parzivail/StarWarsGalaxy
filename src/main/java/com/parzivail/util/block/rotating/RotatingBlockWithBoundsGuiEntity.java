package com.parzivail.util.block.rotating;

import com.parzivail.util.block.VoxelShapeUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.function.BiFunction;

public class RotatingBlockWithBoundsGuiEntity extends RotatingBlockWithGuiEntity
{
	private final VoxelShape shape;

	public RotatingBlockWithBoundsGuiEntity(VoxelShape shape, Settings settings, BiFunction<BlockPos, BlockState, BlockEntity> blockEntitySupplier)
	{
		super(settings.dynamicBounds(), blockEntitySupplier);
		this.shape = shape;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		// East isn't zero, but everything defaults to facing east
		return VoxelShapeUtil.rotate(shape, (state.get(FACING).getHorizontal() + 1) % 4);
	}
}
