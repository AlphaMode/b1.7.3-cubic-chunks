package me.alphamode.world;

import me.alphamode.ext.CubicSaveHandler;
import me.alphamode.world.chunk.CubicChunk;
import me.alphamode.world.chunk.CubicChunkPos;
import me.alphamode.world.chunk.CubicEmptyChunk;
import net.minecraft.class_441;
import net.minecraft.world.Chunk;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.chunk.EmptyChunk;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.save.SaveHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class ThreadedServerChunkCache implements LevelSource {
    private final ForkJoinPool pool = new ForkJoinPool();
    private final LevelSource wrapped;
    private final CubicChunk emptyChunk;
    private final SaveHandler saveHandler;
    private final Map<CubicChunkPos, CubicChunk> cache = new HashMap<>();
    private final Set<CubicChunkPos> toDrop = new HashSet<>();
    private final List<CubicChunk> chunks = new ArrayList<>();
    private final Level level;

    public ThreadedServerChunkCache(Level level, SaveHandler saveHandler, LevelSource generator) {
        this.wrapped = generator;
        this.saveHandler = saveHandler;
        this.emptyChunk = new CubicEmptyChunk(level, new byte[32768], 0, 0, 0);
        this.level = level;
    }

    @Override
    public boolean save(boolean bl, class_441 arg) {
        int n = 0;
        for (int i = 0; i < this.chunks.size(); ++i) {
            Chunk chunk = this.chunks.get(i);
            if (bl && !chunk.field_2376) {
                this.method_2229(chunk);
            }
            if (!chunk.method_630(bl)) continue;
            this.method_2230(chunk);
            chunk.changed = false;
            if (++n != 24 || bl) continue;
            return false;
        }
        if (bl) {
            if (this.saveHandler == null) {
                return true;
            }
            this.saveHandler.method_28();
        }
        return true;
    }

    private void method_2229(Chunk chunk) {
        if (this.saveHandler == null) {
            return;
        }
        try {
            this.saveHandler.method_29(this.level, chunk);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void method_2230(Chunk chunk) {
        if (this.saveHandler == null) {
            return;
        }
        chunk.field_858 = this.level.getTime();
        this.saveHandler.saveChunk(this.level, chunk);
    }

    @Override
    public boolean method_71() {
        for (int i = 0; i < 100; ++i) {
            if (this.toDrop.isEmpty()) continue;
            CubicChunkPos n = this.toDrop.iterator().next();
            Chunk chunk = this.cache.get(n);
            chunk.unload();
            this.method_2230(chunk);
            this.method_2229(chunk);
            this.toDrop.remove(n);
            this.cache.remove(n);
            this.chunks.remove(chunk);
        }
        if (this.saveHandler != null) {
            this.saveHandler.method_25();
        }
        return this.wrapped.method_71();
    }

    @Override
    public boolean supportsSaving() {
        return true;
    }

    @Override
    public String getDebugInfo() {
        return "ThreadedServerChunkCache: " + this.cache.size() + " Drop: " + this.toDrop.size();
    }

    @Override
    public boolean hasChunk(int x, int y, int z) {
        return false;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        CubicLevel.generatingY = y;
        Chunk chunk = this.cache.get(new CubicChunkPos(x, y, z));
        if (chunk == null) {
            if (this.wrapped.isCubic())
                return this.loadChunk(x, y, z);
            Chunk legacyChunk = this.wrapped.getChunk(x, z);
            for (int i = 0; i < 8; i++) {
                CubicChunkPos posToCache = new CubicChunkPos(x, i, z);
                this.toDrop.remove(posToCache);
                CubicChunk cubicChunk = CubicChunk.convert(legacyChunk, i);
                if (cubicChunk.yPos == y)
                    chunk = cubicChunk;
                this.cache.put(posToCache, cubicChunk);
            }
        } else {
            return chunk;
        }
        return chunk == null ? this.loadChunk(x, y, z) : chunk;
    }

    @Override
    public Chunk loadChunk(int x, int y, int z) {
        CubicLevel.generatingY = y;
        CubicChunkPos pos = new CubicChunkPos(x, y, z);
        this.toDrop.remove(pos);
        CubicChunk foundChunk = this.cache.get(pos);
        if (foundChunk == null) {
            foundChunk = this.cubic_method_2231(x, y, z);
            if (foundChunk == null) {
                if (this.wrapped == null) {
                    foundChunk = this.emptyChunk;
                } else {
                    foundChunk = (CubicChunk) this.wrapped.getChunk(x, y, z);
                }
            }

            this.cache.put(pos, foundChunk);
            this.chunks.add(foundChunk);
            if (foundChunk != null) {
                foundChunk.method_641();
                foundChunk.load();
            }

            if (!foundChunk.terrainPopulated &&
                    this.hasChunk(x + 1, y + 1, z + 1) && this.hasChunk(x, y + 1, z + 1) && this.hasChunk(x + 1, y + 1, z) &&
                    this.hasChunk(x + 1, y, z + 1) && this.hasChunk(x, y, z + 1) && this.hasChunk(x + 1, y, z)
            ) {
                this.generate(this, x, y, z);
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
                this.generate(this, x - 1, y, z);
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
                this.generate(this, x, y - 1, z);
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
                this.generate(this, x, y, z - 1);
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
                this.generate(this, x - 1, y, z - 1);
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
                this.generate(this, x - 1, y - 1, z - 1);
            }
        }

        return foundChunk;
    }

    private CubicChunk cubic_method_2231(int x, int y, int z) {
        if (this.saveHandler == null) {
            return null;
        } else {
            try {
                CubicChunk var3 = (CubicChunk) ((CubicSaveHandler) this.saveHandler).getChunk(this.level, x, y, z);
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

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        return this.wrapped.getBiomeProvider(y);
    }

    @Override
    public Dimension getDimension(int y) {
        return this.wrapped.getDimension(y);
    }

    @Override
    public boolean hasChunk(int x, int z) {
        throw throwUnsupported();
    }

    @Override
    public Chunk getChunk(int x, int z) {
        throw throwUnsupported();
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        throw throwUnsupported();
    }

    @Override
    public void generate(LevelSource levelSource, int x, int z) {
        throw throwUnsupported();
    }

    public static RuntimeException throwUnsupported() {
        return new RuntimeException("Unsupported use cubic version!");
    }
}
