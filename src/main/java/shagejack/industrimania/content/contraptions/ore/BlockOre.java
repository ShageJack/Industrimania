package shagejack.industrimania.content.contraptions.ore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shagejack.industrimania.Industrimania;
import shagejack.industrimania.content.worldGen.OreTypeRegistry;
import shagejack.industrimania.content.worldGen.record.OreType;
import shagejack.industrimania.registers.AllBlocks;
import shagejack.industrimania.registers.AllItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class BlockOre extends Block {

    public BlockOre(Properties properties) {
        super(properties);
    }

    public ItemStack getOreChunk(Block block, int count) {
        if (block instanceof BlockOre) {
            String key = block.getRegistryName().toString().split(":")[1];
            return new ItemStack(AllItems.ORE_CHUNKS.get(key).get(), count);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public OreType getOreType(Block block) {
        if (block instanceof BlockOre) {
            String key = block.getRegistryName().toString().split(":")[1].split("_")[2];
            return OreTypeRegistry.oreTypeMap.get(key);
        } else {
            return null;
        }
    }

    public Block getRock(Block block) {
        if (block instanceof BlockOre) {
            String name = "rock_" + block.getRegistryName().toString().split(":")[1].split("_")[1];
            ResourceLocation registryName = new ResourceLocation(Industrimania.MOD_ID, name);
            return ForgeRegistries.BLOCKS.getValue(registryName);
        } else {
            return null;
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (itemStack.getItem() instanceof PickaxeItem) {
            Block block = blockState.getBlock();
            Tier toolTier = ((PickaxeItem) itemStack.getItem()).getTier();
            if (toolTier.getLevel() >= getOreType(block).harvestLevel()) {
                popResource(level, blockPos, getOreChunk(block, 1));
            } else {
                if (TierSortingRegistry.isCorrectTierForDrops(toolTier, getRock(block).defaultBlockState())) {
                    popResource(level, blockPos, new ItemStack(getRock(block)));
                }
            }
        }
    }
}
