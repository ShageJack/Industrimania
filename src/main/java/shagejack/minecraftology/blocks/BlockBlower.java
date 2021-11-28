package shagejack.minecraftology.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shagejack.minecraftology.Minecraftology;
import shagejack.minecraftology.blocks.includes.MCLBlock;
import shagejack.minecraftology.tile.TileEntityClayFurnaceBottom;
import shagejack.minecraftology.util.MCLBlockHelper;
import shagejack.minecraftology.util.MCLMultiBlockCheckHelper;

public class BlockBlower extends MCLBlock {

    public BlockBlower(Material material, String name) {
        super(material, name);
        setHardness(1.0F);
        setHarvestLevel("axe", 1);
        setResistance(4.0F);
        setCreativeTab(Minecraftology.TAB_Minecraftology);
        setHasRotation();
        setRotationType(MCLBlockHelper.RotationType.FOUR_WAY);
        setSoundType(SoundType.GROUND);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumFacing rotation = state.getValue(MCLBlock.PROPERTY_DIRECTION);
        TileEntity tileEntity = null;
        IBlockState stateTube;
        EnumFacing rotationTube;
        switch (rotation) {
            case EAST:
                stateTube = worldIn.getBlockState(pos.add(-1, 0, 0));
                rotationTube = state.getValue(MCLBlock.PROPERTY_DIRECTION);
                if (rotationTube == EnumFacing.WEST || rotationTube == EnumFacing.EAST) {
                    tileEntity = worldIn.getTileEntity(pos.add(-2, -1, 0));
                }
                break;
            case WEST:
                stateTube = worldIn.getBlockState(pos.add(1, 0, 0));
                rotationTube = state.getValue(MCLBlock.PROPERTY_DIRECTION);
                if (rotationTube == EnumFacing.WEST || rotationTube == EnumFacing.EAST) {
                    tileEntity = worldIn.getTileEntity(pos.add(2, -1, 0));
                }
                break;
            case SOUTH:
                stateTube = worldIn.getBlockState(pos.add(0, 0, -1));
                rotationTube = state.getValue(MCLBlock.PROPERTY_DIRECTION);
                if (rotationTube == EnumFacing.NORTH || rotationTube == EnumFacing.SOUTH) {
                    tileEntity = worldIn.getTileEntity(pos.add(0, -1, -2));
                }
                break;
            case NORTH:
                stateTube = worldIn.getBlockState(pos.add(0, 0, 1));
                rotationTube = state.getValue(MCLBlock.PROPERTY_DIRECTION);
                if (rotationTube == EnumFacing.NORTH || rotationTube == EnumFacing.SOUTH) {
                    tileEntity = worldIn.getTileEntity(pos.add(0, -1, 2));
                }
                break;
        }

        if(tileEntity instanceof TileEntityClayFurnaceBottom && ((TileEntityClayFurnaceBottom) tileEntity).checkComplete(worldIn) != -1) {
                ((TileEntityClayFurnaceBottom) tileEntity).blowerInput(worldIn);
                return true;
        }
        return false;
    }



}