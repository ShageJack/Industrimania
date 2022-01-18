package shagejack.industrimania.content.worldGen;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import shagejack.industrimania.api.worldGen.PerlinNoise2D;
import shagejack.industrimania.registers.AllTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author SkyBlade1978
 */
public class Geology {

	private final ArrayList<Block> VANILLA_STONE = Lists.newArrayList(
			Blocks.STONE,
			Blocks.DEEPSLATE,
			Blocks.DIORITE,
			Blocks.ANDESITE,
			Blocks.GRANITE,
			Blocks.INFESTED_STONE,
			Blocks.INFESTED_DEEPSLATE
	);

	private final int LAYER_THICKNESS = 8;

	private final PerlinNoise2D geomeNoiseLayer;
	private final PerlinNoise2D rockNoiseLayer;
	// private final long seed;

	// private final double geomeSize;

	/** random implementation */
	// private static final int multiplier = 1103515245;
	/** random implementation */
	// private static final int addend = 12345;
	/** random implementation */
	// private static final int mask = (1 << 31) - 1;
	/** used to reduce game-time computation by pregenerating random numbers */
	private final short[] whiteNoiseArray;

	/**
	 * 
	 * @param seed
	 *            World seed
	 * @param geomeSize
	 *            Approximate size of rock type layers (should be much bigger than
	 *            <code>rockLayerSize</code>
	 * @param rockLayerSize
	 *            Approximate diameter of layers in the X-Z plane
	 */
	public Geology(long seed, double geomeSize, double rockLayerSize) {
		// this.seed = seed;
		int rockLayerUndertones = 4;
		int undertoneMultiplier = 1 << (rockLayerUndertones - 1);
		geomeNoiseLayer = new PerlinNoise2D(~seed, 128, (float) geomeSize, 2);
		rockNoiseLayer = new PerlinNoise2D(seed, (float) (4 * undertoneMultiplier),
				(float) (rockLayerSize * undertoneMultiplier), rockLayerUndertones);
		// this.geomeSize = geomeSize;

		Random r = new Random(seed);
		whiteNoiseArray = new short[256];
		for (int i = 0; i < whiteNoiseArray.length; i++) {
			whiteNoiseArray[i] = (short) r.nextInt(0x7FFF);
		}
	}

	/**
	 * This method gets the stone replacement for a given coordinate. It does not
	 * check whether there should be stone at the given coordinate, just what
	 * block to put there if there were to be stone at the given coordinate.
	 * 
	 * @param x
	 *            X coordinate (block coordinate space)
	 * @param y
	 *            Y coordinate (block coordinate space)
	 * @param z
	 *            Z coordinate (block coordinate space)
	 * @return A Block object from this mod's registry of stones
	 */
	public Block getStoneAt(int x, int y, int z) {
		// new method: 2D perlin noise instead of 3D
		float geome = geomeNoiseLayer.valueAt(x, z) + y;
		int rv = (int) rockNoiseLayer.valueAt(x, z) + y;
		if (geome < -64) {
			// RockType.IGNEOUS;
			return pickBlockFromList(rv, BlockTags.bind(AllTags.IndustrimaniaTags.igneousStones).getValues());
		} else if (geome < 64) {
			// RockType.METAMORPHIC;
			return pickBlockFromList(rv, BlockTags.bind(AllTags.IndustrimaniaTags.metamorphicStones).getValues());
		} else {
			// RockType.SEDIMENTARY;
			return pickBlockFromList(rv, BlockTags.bind(AllTags.IndustrimaniaTags.sedimentaryStones).getValues());
		}
	}

	public void replaceStoneInChunk(int chunkX, int chunkZ, WorldGenLevel level) {
		ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
		int xOffset = chunkX << 4;
		int zOffset = chunkZ << 4;
		for (int dx = 0; dx < 16; dx++) {
			int x = xOffset | dx;
			for (int dz = 0; dz < 16; dz++) {
				int z = zOffset | dz;
				// int height = 255;//chunk.getHeight(dx, dz);
				// int indexBase = (dx * 16 + dz) * height;
				int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz);// height-1;
				while (y > -64 && level.getBlockState(new BlockPos(x, y, z)).isAir()) {
					y--;
				}
				int baseRockVal = (int) rockNoiseLayer.valueAt(x, z);
				int gbase = (int) geomeNoiseLayer.valueAt(x, z);

				// Block[] column = this.getStoneColumn(xOffset | dx, zOffset | dz, y);
				for (; y > -64; y--) {
					BlockPos coord = new BlockPos(x, y, z);
					// int i = indexBase + y;
					if (VANILLA_STONE.contains(chunk.getBlockState(coord).getBlock())) {
						int geome = gbase + y;
						if (geome < -32) {
							// RockType.IGNEOUS;
							chunk.setBlockState(coord,
									pickBlockFromList(baseRockVal + y, BlockTags.bind(AllTags.IndustrimaniaTags.igneousStones).getValues()).defaultBlockState(), true);
						} else if (geome < 32) {
							// RockType.METAMORPHIC;
							chunk.setBlockState(coord,
									pickBlockFromList(baseRockVal + y, BlockTags.bind(AllTags.IndustrimaniaTags.metamorphicStones).getValues()).defaultBlockState(), true);
						} else {
							// RockType.SEDIMENTARY;
							chunk.setBlockState(coord,
									pickBlockFromList(baseRockVal + y, BlockTags.bind(AllTags.IndustrimaniaTags.sedimentaryStones).getValues()).defaultBlockState(), true);
						}
					}
				}
			}
		}
		//chunk.setModified(true);
	}

	public Block[] getStoneColumn(int x, int z, int height) {
		Block[] col = new Block[height];
		int baseRockVal = (int) rockNoiseLayer.valueAt(x, z);
		double gbase = geomeNoiseLayer.valueAt(x, z);
		for (int y = -64; y < col.length; y++) {
			double geome = gbase + y;
			if (geome < -32) {
				// RockType.IGNEOUS;
				col[y] = pickBlockFromList(baseRockVal + y, BlockTags.bind(AllTags.IndustrimaniaTags.igneousStones).getValues());
			} else if (geome < 32) {
				// RockType.METAMORPHIC;
				col[y] = pickBlockFromList(baseRockVal + y + 3, BlockTags.bind(AllTags.IndustrimaniaTags.metamorphicStones).getValues());
			} else {
				// RockType.SEDIMENTARY;
				col[y] = pickBlockFromList(baseRockVal + y + 5, BlockTags.bind(AllTags.IndustrimaniaTags.sedimentaryStones).getValues());
			}
		}
		return col;
	}

	/**
	 * given any number, this method grabs a block from the list based on that
	 * number.
	 * 
	 * @param value
	 *            product of noise layer + height
	 * @param list
	 * @return
	 */
	private Block pickBlockFromList(int value, List<Block> list) {
		return list.get(whiteNoiseArray[(value / LAYER_THICKNESS) & 0xFF] % list.size());
	}
}
