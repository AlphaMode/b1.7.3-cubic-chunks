package me.alphamode.mixin;

import me.alphamode.util.Util;
import net.minecraft.LoadingScreen;
import net.minecraft.Vec3i;
import net.minecraft.class_763;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Level;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.entity.LocalPlayer;
import net.minecraft.world.entity.Player;
import net.minecraft.world.gen.ChunkCache;
import net.minecraft.world.level.levelgen.LevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public Level level;

    @Shadow public LocalPlayer player;

    @Shadow public abstract void changeDimension(Level level, String string, Player player);

    @Shadow public LoadingScreen loadingScreen;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void changeDimension() {
        System.out.println("Toggling dimension!!");
        if (this.player.dimension == -1) {
            this.player.dimension = 0;
        } else {
            this.player.dimension = -1;
        }

        this.level.removeEntity(this.player);
        this.player.removed = false;
        double playerX = this.player.x;
        double playerY = this.player.y;
        double playerZ = this.player.z;
        double var5 = 8.0;
        Level var7;
        if (this.player.dimension == -1) {
            playerX /= var5;
            playerY /= var5;
            playerZ /= var5;
            this.player.moveTo(playerX, playerY, playerZ, this.player.yRot, this.player.xRot);
            if (this.player.isAlive()) {
                this.level.method_253(this.player, false);
            }

            var7 = null;
            var7 = new Level(this.level, Dimension.getDimensionFromId(-1));
            this.changeDimension(var7, "Entering the Nether", this.player);
        } else {
            playerX *= var5;
            playerY *= var5;
            playerZ *= var5;
            this.player.moveTo(playerX, playerY, playerZ, this.player.yRot, this.player.xRot);
            if (this.player.isAlive()) {
                this.level.method_253(this.player, false);
            }

            var7 = null;
            var7 = new Level(this.level, Dimension.getDimensionFromId(0));
            this.changeDimension(var7, "Leaving the Nether", this.player);
        }

        this.player.level = this.level;
        if (this.player.isAlive()) {
            this.player.moveTo(playerX, playerX, playerZ, this.player.yRot, this.player.xRot);
            this.level.method_253(this.player, false);
            (new class_763()).method_2251(this.level, this.player);
        }

    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void setLoadingStage(String string) {
        this.loadingScreen.setHeader(string);
        this.loadingScreen.setMessage("Building terrain");
        short range = 128;
        int var3 = 0;
        int var4 = range * 2 / 16 + 1;
        var4 *= var4;
        LevelSource var5 = this.level.getLevelSource();
        Vec3i spawn = this.level.method_1599();
        if (this.player != null) {
            spawn.x = (int)this.player.x;
            spawn.z = (int)this.player.z;
        }

        if (var5 instanceof ChunkCache) {
            ChunkCache var7 = (ChunkCache)var5;
            var7.setCenter(spawn.x >> 4, spawn.z >> 4);
        }

        for(int chunkX = -range; chunkX <= range; chunkX += 16) {
            for(int chunkY = -range; chunkY <= range; chunkY += 16) {
                for (int chunkZ = -range; chunkZ <= range; chunkZ += 16) {
                    this.loadingScreen.method_1235(var3++ * 100 / var4);
                    int finalChunkX = chunkX;
                    int finalChunkY = chunkY;
                    int finalChunkZ = chunkZ;
                    Util.backgroundExecutor().execute(() -> this.level.getChunk(spawn.x + finalChunkX, spawn.y + finalChunkY, spawn.z + finalChunkZ));

                    while (this.level.method_295()) {
                    }
                }
            }
        }

        this.loadingScreen.setMessage("Simulating world for a bit");
        var4 = 2000;
        this.level.method_310();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean method_1075() {
        return true;
    }
}
