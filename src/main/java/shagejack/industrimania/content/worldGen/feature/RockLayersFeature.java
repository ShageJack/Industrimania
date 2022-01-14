package shagejack.industrimania.content.worldGen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.antlr.runtime.misc.IntArray;
import shagejack.industrimania.api.worldGen.OpenSimplexNoise;
import shagejack.industrimania.content.worldGen.RockLayer;
import shagejack.industrimania.registers.AllBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RockLayersFeature extends Feature<NoneFeatureConfiguration> {

    private final ArrayList<Block> VANILLA_STONE = Lists.newArrayList(
            Blocks.STONE,
            Blocks.DEEPSLATE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.GRANITE,
            Blocks.INFESTED_STONE,
            Blocks.INFESTED_DEEPSLATE
    );

    private final ArrayList<RockLayer> ROCK_LAYERS = Lists.newArrayList(
            //Sedimentary rocks
            //Metamorphic rocks
            new RockLayer(Blocks.DEEPSLATE, -32, 32, 16),
            //Igneous rocks
            new RockLayer(Blocks.ANDESITE, 32),
            new RockLayer(Blocks.GRANITE, -64, 0),
            new RockLayer(Blocks.DIORITE, -64, 0)
    );

    public RockLayersFeature(Codec p_65786_) {
        super(p_65786_);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> f) {
        if (f.chunkGenerator() instanceof FlatLevelSource) {
            return false;
        }

        WorldGenLevel level = f.level();
        ChunkPos cp = new ChunkPos(f.origin());

        OpenSimplexNoise noiseMap = new OpenSimplexNoise(level.getSeed());

        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {

                    double FEATURE_SIZE = ROCK_LAYERS.size() * 32;

                    double value = noiseMap.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, 0.0);
                    ArrayList<RockLayer> TEMP = (ArrayList<RockLayer>) ROCK_LAYERS.clone();
                    Collections.shuffle(TEMP, new Random((long) (value + 1) / 2 * ROCK_LAYERS.size()));



                    for(RockLayer rockLayer : TEMP) {
                        Block rock = rockLayer.getRock();
                        int minY = rockLayer.getMinY();
                        int maxY = rockLayer.getMaxY();
                        int thickness = rockLayer.getThickness();

                        BlockPos p = new BlockPos(x, y, z);
                        BlockState state = level.getBlockState(p);

                        Random rnd = new Random((long) (value + 1) / 2 * ROCK_LAYERS.size());
                        if (maxY - minY - thickness > 0) {
                            int offset = rnd.nextInt(maxY - minY - thickness);

                            if (y > minY + offset && y < minY + offset + thickness) {
                                if (VANILLA_STONE.contains(state.getBlock())) {
                                    level.setBlock(p, rock.defaultBlockState(), 2 | 16);
                                }
                            }
                        }
                    }
                }
            }
        }

        //Replace Remaining Vanilla Stone
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {

                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(p);

                    if (VANILLA_STONE.contains(state.getBlock())) {
                        level.setBlock(p, ROCK_LAYERS.get(0).getRock().defaultBlockState(), 2 | 16);
                    }
                }
            }
        }

        return true;
    }


}