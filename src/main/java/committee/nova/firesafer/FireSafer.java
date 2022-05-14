package committee.nova.firesafer;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.function.BiConsumer;

import static net.minecraft.world.entity.monster.Creeper.DATA_IS_IGNITED;

@Mod(FireSafer.MODID)
public class FireSafer {
    public static final String MODID = "firesafer";
    private final HashMap<Item, BiConsumer<Level, Vec3>> entityBucket = new HashMap<>();

    public FireSafer() {
        initBuckets();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onExtension(FireSafetyExtensionEvent event) {
        event.addExtinguishable((short) 13468, new FireSafetyApi.ExtinguishableBlock(
                (w, p) -> w.getBlockState(p).is(Blocks.LAVA),
                (w, p) -> Blocks.MAGMA_BLOCK.defaultBlockState(),
                (w, p) -> {
                }
        ));
        event.addExtinguishable((short) 13468, new FireSafetyApi.ExtinguishableEntity(
                (w, e) -> e.getType().equals(EntityType.TNT),
                (w, e) -> {
                    w.addFreshEntity(new ItemEntity(w, e.getX(), e.getY() + 0.2D, e.getZ(), new ItemStack(Items.TNT)));
                    e.kill();
                }
        ));
        event.addExtinguishable((short) 13467, new FireSafetyApi.ExtinguishableEntity(
                (w, e) -> e instanceof Creeper && e.getEntityData().get(DATA_IS_IGNITED),
                (w, e) -> {
                    e.getEntityData().set(DATA_IS_IGNITED, false);
                    final var c = (Creeper) e;
                    c.setSwellDir(0);
                    c.maxSwell = 5000;
                }
        ));
        event.addFireFightingWaterItem((short) 13468, new FireSafetyApi.FireFightingWaterContainerItem(
                (p, s) -> entityBucket.containsKey(s.getItem()),
                (p, s) -> 800,
                (p, a, s) -> new ItemStack(Items.BUCKET),
                (p, a, s) -> {
                    for (final var m : entityBucket.entrySet())
                        if (m.getKey().equals(s.getItem())) m.getValue().accept(p.level, p.position());
                }
        ));
    }

    private void initBuckets() {
        entityBucket.put(Items.SALMON_BUCKET, (l, pos) -> pourEntity(l, pos, new Salmon(EntityType.SALMON, l)));
        entityBucket.put(Items.AXOLOTL_BUCKET, (l, pos) -> pourEntity(l, pos, new Axolotl(EntityType.AXOLOTL, l)));
        entityBucket.put(Items.COD_BUCKET, (l, pos) -> pourEntity(l, pos, new Cod(EntityType.COD, l)));
        entityBucket.put(Items.PUFFERFISH_BUCKET, (l, pos) -> pourEntity(l, pos, new Pufferfish(EntityType.PUFFERFISH, l)));
        entityBucket.put(Items.TROPICAL_FISH_BUCKET, (l, pos) -> pourEntity(l, pos, new TropicalFish(EntityType.TROPICAL_FISH, l)));
    }

    private void pourEntity(Level l, Vec3 pos, Entity e) {
        e.setPos(pos.add(0, .5, 0));
        l.addFreshEntity(e);
    }
}
