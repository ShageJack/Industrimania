package shagejack.industrimania.content.pollution;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.system.CallbackI;
import shagejack.industrimania.content.pollution.protection.PollutionProtectiveArmor;
import shagejack.industrimania.registers.item.AllItems;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class PollutionEventHandler {

    private static final int IDLE = 1200;
    private static final long SPREAD_LIMIT = 400000;
    private static final long DECAY_LIMIT = 500000;
    private static final long PARTICLE_LIMIT = 750000;
    private static final long EFFECT_LIMIT = 750000;
    private static final long DAMAGE_LIMIT = 1000000;
    private static final long ACID_RAIN_LIMIT = 2000000;
    private static final long DECAY_DIVIDEND = 25000;
    private static final long FOG_EFFECT_LIMIT = 500000;
    private static final int DECAY_SUBTRAHEND= 20;

    private static int tickCounter;

    @SubscribeEvent
    public static void tickPollution(TickEvent.WorldTickEvent event) {
        if (tickCounter == IDLE) {
            //Tick Polluted Chunks
            PollutionDataHooks.pollutionMap.forEach( (chunkRef, pollution) -> {
                if (event.world.dimensionType() == chunkRef.dimension()) {
                    if (pollution.amount != 0) {
                        DimensionType dim = chunkRef.dimension();
                        int chunkX = chunkRef.pos().x;
                        int chunkZ = chunkRef.pos().z;
                        AABB chunkRange = new AABB(chunkRef.pos().getMinBlockX(), chunkRef.dimension().minY(), chunkRef.pos().getMinBlockZ(), chunkRef.pos().getMaxBlockX(), chunkRef.dimension().height(), chunkRef.pos().getMaxBlockZ());
                        //Reduce Pollution
                        pollution.reducePollution();
                        //Manage Spread
                        if (pollution.getAmount() > SPREAD_LIMIT) {
                            pollution.spread(PollutionDataHooks.getPollution(dim, chunkX + 1, chunkZ));
                            pollution.spread(PollutionDataHooks.getPollution(dim, chunkX, chunkZ + 1));
                            pollution.spread(PollutionDataHooks.getPollution(dim, chunkX - 1, chunkZ));
                            pollution.spread(PollutionDataHooks.getPollution(dim, chunkX, chunkZ - 1));
                        }
                        //TODO: Render Particles
                        if (pollution.getAmount() > PARTICLE_LIMIT) {
                            //event.world.addParticle(ParticleTypes.SMOKE, chunkRef.pos().getMinBlockX(), chunkRef.dimension().minY(), chunkRef.pos().getMinBlockZ(), chunkRef.pos().getMaxBlockX(), Math.min(chunkRef.dimension().height(), chunkRef.dimension().minY() + pollution.getAmount() / 10000), chunkRef.pos().getMaxBlockZ());
                        }
                        //Damage Blocks
                        if (pollution.getAmount() > DECAY_LIMIT) {
                            for (int i = DECAY_SUBTRAHEND; i < pollution.getAmount() / DECAY_DIVIDEND; i++) {
                                int x = chunkRef.pos().getMinBlockX() + event.world.getRandom().nextInt(16);
                                int y = chunkRef.dimension().minY() + event.world.getRandom().nextInt(chunkRef.dimension().height());
                                int z = chunkRef.pos().getMinBlockZ() + event.world.getRandom().nextInt(16);
                                pollution.damageBlock(event.world, new BlockPos(x, y, z), pollution.getAmount() > ACID_RAIN_LIMIT);
                            }
                        }
                        //Manage Entities
                        for (Entity entity : event.world.getEntities((Entity) null, chunkRange, Entity::isAlive)) {
                            if (entity.chunkPosition().equals(chunkRef.pos())) {
                                if (entity instanceof LivingEntity) {
                                    if (entity instanceof Player) {
                                        hurtArmor((Player) entity, (float) (2.0F + 0.5F * Math.floor((double) pollution.getAmount() / 100000)));
                                        if (pollution.getAmount() > DAMAGE_LIMIT) {
                                            if (getProtectionLevel((Player) entity) < 1 + pollution.getAmount() / 2000000) {
                                                pollution.damageEntity((LivingEntity) entity);
                                            }
                                        }
                                        if (pollution.getAmount() > EFFECT_LIMIT) {
                                            if (getProtectionLevel((Player) entity) < 1 + pollution.getAmount() / 2000000) {
                                                pollution.giveEffect(event.world, (LivingEntity) entity);
                                            }
                                        }
                                        if (pollution.getAmount() > FOG_EFFECT_LIMIT) {
                                            if (getProtectionLevel((Player) entity) < 1 + pollution.getAmount() / 2000000) {
                                                pollution.giveFogEffect(event.world, (LivingEntity) entity);
                                            }
                                        }
                                    } else {
                                        if (pollution.getAmount() > DAMAGE_LIMIT) {
                                            pollution.damageEntity((LivingEntity) entity);
                                        }
                                        if (pollution.getAmount() > EFFECT_LIMIT) {
                                            pollution.giveEffect(event.world, (LivingEntity) entity);
                                        }
                                        if (pollution.getAmount() > FOG_EFFECT_LIMIT) {
                                            pollution.giveFogEffect(event.world, (LivingEntity) entity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    @SubscribeEvent
    public static void chunkLoad(ChunkDataEvent.Load event) {
        if (event.getWorld() != null) {
            PollutionDataHooks.readData(event.getWorld().dimensionType(), event.getChunk().getPos().x, event.getChunk().getPos().z, event.getData());
        }
    }

    @SubscribeEvent
    public static void chunkSave(ChunkDataEvent.Save event) {
        if (event.getWorld() != null) {
            PollutionDataHooks.saveData(event.getWorld().dimensionType(), event.getChunk().getPos().x, event.getChunk().getPos().z, event.getData());
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event) {
        if (event.getWorld() != null) {
            PollutionDataHooks.clearData(event.getWorld().dimensionType(), event.getChunk().getPos().x, event.getChunk().getPos().z);
        }
    }

    @SubscribeEvent
    public static void onFogColorRender(EntityViewRenderEvent.FogColors event) {
        Entity entity = event.getCamera().getEntity();
        if (entity instanceof Player) {
            if (getProtectionLevel((Player) entity) < 16) {
                PollutionDataHooks.pollutionMap.forEach((chunkRef, pollution) -> {
                            if (pollution.amount != 0) {
                                if (entity.level.dimensionType() == chunkRef.dimension()) {
                                    if (entity.chunkPosition().equals(chunkRef.pos())) {
                                        if (entity.chunkPosition().equals(chunkRef.pos())) {
                                            Color color = pollution.getFogColor();
                                            event.setRed((float) color.getRed() / 255F);
                                            event.setBlue((float) color.getBlue() / 255F);
                                            event.setGreen((float) color.getGreen() / 255F);
                                        }
                                    }
                                }
                            }
                        }
                );
            }
        }
    }

    @SubscribeEvent
    public static void onFogDensityRender(EntityViewRenderEvent.FogDensity event) {
        Entity entity = event.getCamera().getEntity();
        if (entity instanceof Player) {
            if (getProtectionLevel((Player) entity) < 16) {
                PollutionDataHooks.pollutionMap.forEach((chunkRef, pollution) -> {
                            if (pollution.amount != 0) {
                                if (entity.level.dimensionType() == chunkRef.dimension()) {
                                    if (entity.chunkPosition().equals(chunkRef.pos())) {
                                        if (entity.chunkPosition().equals(chunkRef.pos())) {
                                                event.setDensity(pollution.getFogDensity());
                                                event.setCanceled(true);
                                        }
                                    }
                                }
                            }
                        }
                );
            }
        }
    }

    public static int getProtectionLevel(Player player) {
        NonNullList<ItemStack> armors = player.getInventory().armor;
        if (armors.get(3).is(AllItems.creativeHazardProtectiveHelmet.get())) return 1073741824;
        if (armors.get(0).is(AllItems.hazardProtectiveBoots.get()) && armors.get(1).is(AllItems.hazardProtectiveLeggings.get()) && armors.get(2).is(AllItems.hazardProtectiveChestplate.get()) && armors.get(3).is(AllItems.hazardProtectiveHelmet.get())) return 1;
        return 0;
    }

    public static void hurtArmor(Player player, float amount) {
        Inventory inv = player.getInventory();
        NonNullList<ItemStack> armors = inv.armor;
        Set<Integer> slotsToHurt = new HashSet<>();
        for (int i = 0; i < armors.size(); i++) {
            if (armors.get(i).getItem() instanceof PollutionProtectiveArmor) {
                slotsToHurt.add(i);
            }
        }
        Object[] objArr = slotsToHurt.toArray();
        int[] arr = new int[objArr.length];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = (int) objArr[i];
        }
        inv.hurtArmor(DamageSource.OUT_OF_WORLD, amount, arr);
    }

}
