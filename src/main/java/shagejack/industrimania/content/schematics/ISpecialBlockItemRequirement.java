package shagejack.industrimania.content.schematics;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ISpecialBlockItemRequirement {

	default ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.INVALID;
	}

}
