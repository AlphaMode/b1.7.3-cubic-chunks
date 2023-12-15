package me.alphamode.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.gen.CubicLevelSource;
import me.alphamode.world.CubicChunk;
import me.alphamode.world.CubicChunkPos;
import net.minecraft.Vec3i;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.ServerChunkCache;
import net.minecraft.world.entity.Player;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.save.SaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin implements CubicLevelSource {


    @Shadow
    private LevelSource wrapped;
    @Shadow
    private Chunk emptyChunk;
    @Shadow
    private List<Chunk> field_3348;

    @Shadow
    protected abstract void method_2230(Chunk chunk);

    @Shadow
    protected abstract void method_2229(Chunk chunk);

    @Shadow
    private SaveHandler saveHandler;
    @Shadow
    private Level level;
    private Map<CubicChunkPos, Chunk> cubic$cache = new HashMap<>();
    private Set<CubicChunkPos> cubic$toDrop = new HashSet<>();

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return this.cubic$cache.containsKey(new CubicChunkPos(x, y, z));
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        Chunk chunk = this.cubic$cache.get(new CubicChunkPos(x, y, z));
        if (chunk == null) {
            if (this.wrapped.isCubic())
                return this.loadChunk(x, y, z);
            Chunk legacyChunk = this.wrapped.getChunk(x, z);
            for (int i = 0; i < 8; i++) {
                CubicChunkPos posToCache = new CubicChunkPos(x, i, z);
                this.cubic$toDrop.remove(posToCache);
                CubicChunk cubicChunk = CubicChunk.convert(legacyChunk, i);
                if (cubicChunk.yPos == y)
                    chunk = cubicChunk;
                this.cubic$cache.put(posToCache, cubicChunk);
            }
        } else {
            return chunk;
        }
        return chunk == null ? this.loadChunk(x, y, z) : chunk;
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        CubicChunkPos pos = new CubicChunkPos(x, y, z);
        this.cubic$toDrop.remove(pos);
        Chunk foundChunk = this.cubic$cache.get(pos);
        if (foundChunk == null) {
            foundChunk = this.cubic_method_2231(x, y, z);
            if (foundChunk == null) {
                if (this.wrapped == null) {
                    foundChunk = this.emptyChunk;
                } else {
                    foundChunk = this.wrapped.getChunk(x, y, z);
                }
            }

            this.cubic$cache.put(pos, foundChunk);
            this.field_3348.add(foundChunk);
            if (foundChunk != null) {
                foundChunk.method_641();
                foundChunk.method_643();
            }

            if (!foundChunk.terrainPopulated &&
                    this.hasChunk(x + 1, y + 1, z + 1) && this.hasChunk(x, y + 1, z + 1) && this.hasChunk(x + 1, y + 1, z) &&
                    this.hasChunk(x + 1, y, z + 1) && this.hasChunk(x, y, z + 1) && this.hasChunk(x + 1, y, z)
            ) {
                this.generate((LevelSource) this, x, y, z);
            }

            if (this.hasChunk(x - 1, y, z)
                    && !this.getChunk(x - 1, y, z).terrainPopulated
                    && this.hasChunk(x - 1, y, z + 1)
                    && this.hasChunk(x, y, z + 1)
                    && this.hasChunk(x - 1, y, z)
                    && this.hasChunk(x - 1, y + 1, z + 1)
                    && this.hasChunk(x, y + 1, z + 1)
                    && this.hasChunk(x - 1, y + 1, z)
            ) {
                this.generate((LevelSource) this, x - 1, y, z);
            }

            if (this.hasChunk(x, y - 1, z)
                    && !this.getChunk(x, y - 1, z).terrainPopulated
                    && this.hasChunk(x + 1, y - 1, z)
                    && this.hasChunk(x, y - 1, z)
                    && this.hasChunk(x + 1, y, z)
                    && this.hasChunk(x + 1, y - 1, z - 1)
                    && this.hasChunk(x, y - 1, z - 1)
                    && this.hasChunk(x + 1, y, z + 1)
            ) {
                this.generate((LevelSource) this, x, y - 1, z);
            }

            if (this.hasChunk(x, y, z - 1)
                    && !this.getChunk(x, y, z - 1).terrainPopulated
                    && this.hasChunk(x + 1, y, z - 1)
                    && this.hasChunk(x, y, z - 1)
                    && this.hasChunk(x + 1, y, z)
                    && this.hasChunk(x + 1, y - 1, z - 1)
                    && this.hasChunk(x, y - 1, z - 1)
                    && this.hasChunk(x + 1, y - 1, z)
            ) {
                this.generate((LevelSource) this, x, y, z - 1);
            }

            if (this.hasChunk(x - 1, y, z - 1)
                    && !this.getChunk(x - 1, y, z - 1).terrainPopulated
                    && this.hasChunk(x - 1, y, z - 1)
                    && this.hasChunk(x, y, z - 1)
                    && this.hasChunk(x - 1, y, z)
                    && this.hasChunk(x - 1, y + 1, z - 1)
                    && this.hasChunk(x, y + 1, z - 1)
                    && this.hasChunk(x - 1, y + 1, z)
            ) {
                this.generate((LevelSource) this, x - 1, y, z - 1);
            }

            if (this.hasChunk(x - 1, y - 1, z - 1)
                    && !this.getChunk(x - 1, y - 1, z - 1).terrainPopulated
                    && this.hasChunk(x - 1, y - 1, z - 1)
                    && this.hasChunk(x, y, z - 1)
                    && this.hasChunk(x - 1, y, z)
                    && this.hasChunk(x - 1, y - 1, z)
                    && this.hasChunk(x, y, z - 1)
                    && this.hasChunk(x - 1, y, z)
            ) {
                this.generate((LevelSource) this, x - 1, y - 1, z - 1);
            }
        }

        return foundChunk;
    }

    @Override
    public void generate(LevelSource levelSource, int x, int y, int z) {
        Chunk chunk = this.getChunk(x, y, z);
        if (!chunk.terrainPopulated) {
            chunk.terrainPopulated = true;
            if (this.wrapped != null) {
                if (this.wrapped.isCubic())
                    this.wrapped.generate(levelSource, x, y, z);
                else
                    this.wrapped.generate(levelSource, x, z);
                chunk.setChanged();
            }
        }
    }

    private int culv = 0;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean method_71() {
        for (int var1 = 0; var1 < 100; ++var1) {
            if (!this.cubic$toDrop.isEmpty()) {
                CubicChunkPos pos = this.cubic$toDrop.iterator().next();
                Chunk chunk = this.cubic$cache.get(pos);
                chunk.method_645();
                this.method_2230(chunk);
                this.method_2229(chunk);
                this.cubic$toDrop.remove(pos);
                this.cubic$cache.remove(pos);
                this.field_3348.remove(chunk);
            }
        }

        // From Reindev because I didn't feel like thinking this through
//        for(int j = 0; j < 100; ++j) {
//            if (this.culv >= this.field_3348.size()) {
//                this.culv = 0;
//                break;
//            }
//
//            CubicChunk chunk = (CubicChunk) this.field_3348.get(this.culv++);
//            Player entityplayer = this.level
//                    .method_210((double)(chunk.xPos << 4) + 8.0, (double)(chunk.yPos << 4) + 8.0, (double)(chunk.zPos << 4) + 8.0, 288.0);
//            if (entityplayer == null && !chunk.markedToUnload) {
//                this.unloadChunksIfNotNearSpawn(chunk, chunk.xPos, chunk.yPos, chunk.zPos);
//            }
//        }

        if (this.saveHandler != null) {
            this.saveHandler.method_25();
        }

        return this.wrapped.method_71();
    }

    public void unloadChunksIfNotNearSpawn(CubicChunk chunk, int x, int y, int z) {
        CubicChunkPos pos = new CubicChunkPos(x, y, z);
        if (this.level.dimension.stayInDimensionOnDeath()) {
            Vec3i var3 = this.level.method_1599();
            int posX = x * 16 + 8 - var3.x;
            int posY = y * 16 + 8 - var3.y;
            int posZ = z * 16 + 8 - var3.z;
            int range = 128;
            if ((posX < -range || posX > range || posY < -range || posY > range || posZ < -range || posZ > range)
                    && !this.cubic$toDrop.contains(pos)) {
                this.cubic$toDrop.add(pos);
                chunk.markedToUnload = true;
            }
        } else {
            this.cubic$toDrop.add(pos);
            chunk.markedToUnload = true;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public String getDebugInfo() {
        return "ServerChunkCache: " + this.cubic$cache.size() + " Drop: " + this.cubic$toDrop.size();
    }

    private Chunk cubic_method_2231(int x, int y, int z) {
        if (this.saveHandler == null) {
            return null;
        } else {
            try {
                Chunk var3 = ((CubicSaveHandler) this.saveHandler).getChunk(this.level, x, y, z);
                if (var3 != null) {
                    var3.field_858 = this.level.getTime();
                }

                return var3;
            } catch (Exception var4) {
                var4.printStackTrace();
                return null;
            }
        }
    }
}
