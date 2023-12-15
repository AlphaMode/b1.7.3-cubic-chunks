package me.alphamode.pattern;

import java.util.function.Predicate;

import me.alphamode.util.BlockPos;
import net.minecraft.LevelReader;
import net.minecraft.world.tile.entity.TileEntity;
import javax.annotation.Nullable;

public class BlockInWorld {
    private final LevelReader level;
    private final BlockPos pos;
    private final boolean loadChunks;
    private int tile = -1;
    private TileEntity entity;
    private boolean cachedEntity;

    public BlockInWorld(LevelReader levelReader, BlockPos blockPos, boolean loadChunks) {
        this.level = levelReader;
        this.pos = blockPos;
        this.loadChunks = loadChunks;
    }

    public int getTile() {
        if (this.tile == -1 && this.loadChunks) {
            this.tile = this.level.getTile(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        return this.tile;
    }

    @Nullable
    public TileEntity getEntity() {
        if (this.entity == null && !this.cachedEntity) {
            this.entity = this.level.getTileEntity(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            this.cachedEntity = true;
        }

        return this.entity;
    }

    public LevelReader getLevel() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public static Predicate<BlockInWorld> hasState(Predicate<Integer> predicate) {
        return blockInWorld -> blockInWorld != null && predicate.test(blockInWorld.getTile());
    }
}