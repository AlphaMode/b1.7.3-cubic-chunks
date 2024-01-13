package me.alphamode.mixin;

import me.alphamode.ext.DimensionExt;
import me.alphamode.ext.LevelExt;
import me.alphamode.world.ThreadedServerChunkCache;
import me.alphamode.world.chunk.CubicChunk;
import me.alphamode.world.chunk.CubicChunkPos;
import me.alphamode.world.CubicLevel;
import net.minecraft.LevelSaver;
import net.minecraft.LightUpdater;
import net.minecraft.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightingBolt;
import net.minecraft.world.entity.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.BiomeProvider;
import net.minecraft.world.level.levelgen.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.save.SaveHandler;
import net.minecraft.world.tile.Tile;
import net.minecraft.world.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.*;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelExt {
    @Shadow
    public List<Entity> entities;

    @Shadow
    @Final
    public Dimension dimension;

    @Shadow
    protected LevelSource levelSource;

    @Shadow
    private List<Entity> field_285;

    @Shadow
    public List<Player> players;

    @Shadow
    public abstract void updateSleepingPlayerList();

    @Shadow
    protected abstract void method_272(Entity entity);

    @Shadow
    private Set<CubicChunkPos> field_283;

    @Shadow
    private int field_284;

    @Shadow
    protected int field_292;

    @Shadow
    public abstract void playSound(double d, double e, double f, String string, float g, float h);

    @Shadow
    public Random random;

    private final ExecutorService lightingThread = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());


    /**
     * @author
     * @reason
     */
    @Overwrite
    public BiomeProvider getBiomeProvider() {
        return this.levelSource.getDimension(CubicLevel.generatingY).biomeProvider;
    }

    @Shadow
    public abstract boolean isRaining();

    @Shadow
    public abstract boolean isThundering();

    @Shadow
    public abstract Player getNearestPlayer(double d, double e, double f, double g);

    @Shadow
    protected int field_3013;

    @Shadow
    public abstract boolean isRainingAt(int x, int y, int z);

    @Shadow
    public abstract boolean addGlobalEntity(Entity entity);

    @Shadow
    public List<Entity> globalEntities;

    @Shadow
    private List<Entity> field_274;

    @Shadow
    public abstract void method_290(Entity entity);

    @Shadow
    protected abstract void method_279(Entity entity);

    @Shadow
    private boolean field_3455;

    @Shadow
    public List<TileEntity> field_3456;

    @Shadow
    private List<TileEntity> field_3454;

    @Shadow
    public abstract void method_299(int i, int j, int k);

    @Shadow
    public boolean field_2818;

    @Shadow
    protected LevelData levelData;

    @Shadow
    private ArrayList<AABB> field_281;

    @Shadow
    public int skyDarken;

    @Shadow
    public abstract void method_263(int i, int j, int k, int l, int m, int n);

    @Shadow
    protected List<LevelListener> listeners;

    @Shadow
    private static int field_310;

    @Shadow
    private List<LightUpdater> lightingUpdates;

    private final List<LightUpdater> cubic$lightingUpdates = new ArrayList<>();

    @Shadow
    public abstract boolean setTileWithUpdate(int i, int j, int k, int l);

    @Shadow
    @Final
    protected LevelSaver levelSaver;

    @Shadow private long field_277;

    @Shadow public abstract float getRainLevel(float f);

    @Shadow public abstract float getThunderLevel(float f);

    @Shadow private int field_282;

    @Inject(method = "method_286", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ((DimensionExt) this.dimension).tick();
    }

    /**
     * @author AlphaMode
     * @reason Deprecated
     */
    @Overwrite
    public Chunk getChunkFromPos(int x, int z) {
        throw new RuntimeException("Use cubic version!");
    }


    /**
     * @author AlphaMode
     * @reason Deprecated
     */
    @Overwrite
    public Chunk getChunk(int x, int z) {
        throw new RuntimeException("Use cubic version!");
    }

    /**
     * @author AlphaMode
     * @reason Deprecated
     */
    @Overwrite
    private boolean hasChunk(int x, int z) {
        throw new RuntimeException("Use cubic version!");
    }

    private boolean hasChunk(int x, int y, int z) {
        return this.levelSource.hasChunk(x, y, z);
    }

    /**
     * @author AlphaMode
     * @reason Use cubic chunk
     */
    @Overwrite
    public boolean isLoaded(int x, int y, int z) {
        return this.hasChunk(x >> 4, y >> 4, z >> 4);
    }

    public Chunk getChunkFromPos(int x, int y, int z) {
        return this.getChunk(x >> 4, y >> 4, z >> 4);
    }

    public Chunk getChunk(int x, int y, int z) {
        return this.levelSource.getChunk(x, y, z);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public List<Entity> getEntities(Entity entity, AABB aABB) {
        this.field_285.clear();
        int minX = Mth.floor((aABB.x0 - 2.0) / 16.0);
        int maxX = Mth.floor((aABB.x1 + 2.0) / 16.0);
        int minY = Mth.floor((aABB.y0 - 2.0) / 16.0);
        int maxY = Mth.floor((aABB.y1 + 2.0) / 16.0);
        int minZ = Mth.floor((aABB.z0 - 2.0) / 16.0);
        int maxZ = Mth.floor((aABB.z1 + 2.0) / 16.0);

        for (int chunkX = minX; chunkX <= maxX; ++chunkX) {
            for (int chunkY = minY; chunkY <= maxY; ++chunkY) {
                for (int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ) {
                    if (this.hasChunk(chunkX, chunkY, chunkZ)) {
                        this.getChunk(chunkX, chunkY, chunkZ).getEntities(entity, aABB, this.field_285);
                    }
                }
            }
        }

        return this.field_285;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public List<Entity> getEntitiesOfClass(Class<? extends Entity> class_, AABB aABB) {
        int minX = Mth.floor((aABB.x0 - 2.0) / 16.0);
        int maxX = Mth.floor((aABB.x1 + 2.0) / 16.0);
        int minY = Mth.floor((aABB.x0 - 2.0) / 16.0);
        int maxY = Mth.floor((aABB.x1 + 2.0) / 16.0);
        int minZ = Mth.floor((aABB.z0 - 2.0) / 16.0);
        int maxZ = Mth.floor((aABB.z1 + 2.0) / 16.0);
        List<Entity> entities = new ArrayList<>();

        for (int chunkX = minX; chunkX <= maxX; ++chunkX) {
            for (int chunkY = minY; chunkY <= maxY; ++chunkY) {
                for (int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ) {
                    if (this.hasChunk(chunkX, chunkY, chunkZ)) {
                        this.getChunk(chunkX, chunkY, chunkZ).getEntitiesOfClass(class_, aABB, entities);
                    }
                }
            }
        }

        return entities;
    }

    @Redirect(method = "setChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getChunkFromPos(II)Lnet/minecraft/world/Chunk;"))
    private Chunk useCubicVersion(Level instance, int j, int i, int x, int y, int z) {
        return getChunkFromPos(x, y, z);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getTile(int x, int y, int z) {
        return this.getChunk(x >> 4, y >> 4, z >> 4).getTile(x & 15, y & 15, z & 15);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getTileMeta(int x, int y, int z) {
        Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
        x &= 15;
        y &= 15;
        z &= 15;
        return chunk.getMeta(x, y, z);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getYHeight(int x, int z) {
        if (!this.hasChunk(x >> 4, 0, z >> 4)) {
            return 0;
        } else {
            CubicChunk chunk = (CubicChunk) this.getChunk(x >> 4, 0, z >> 4);
            return chunk.getYHeight(x & 15, z & 15);
        }
    }

    @Override
    public int getYHeight(int x, int y, int z) {
        if (!this.hasChunk(x >> 4, y >> 4, z >> 4)) {
            return 0;
        } else {
            Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
            return chunk.getYHeight(x & 15, z & 15);
        }
    }

    @Override
    public BiomeProvider getBiomeProvider(int y) {
        return this.levelSource.getBiomeProvider(y);
    }

    public float getTimeOfDay(float partialTick, int y) {
        return this.levelSource.getDimension(y).getTimeOfDay(this.levelData.getTime(), partialTick);
    }

    public Vec3 getCloudColor(float partialTick, int y) {
        float f2;
        float f3;
        float f4 = this.getTimeOfDay(partialTick, y);
        float f5 = Mth.cos(f4 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        if (f5 < 0.0f) {
            f5 = 0.0f;
        }
        if (f5 > 1.0f) {
            f5 = 1.0f;
        }
        float f6 = (float)(this.field_277 >> 16 & 0xFFL) / 255.0f;
        float f7 = (float)(this.field_277 >> 8 & 0xFFL) / 255.0f;
        float f8 = (float)(this.field_277 & 0xFFL) / 255.0f;
        float f9 = this.getRainLevel(partialTick);
        if (f9 > 0.0f) {
            f3 = (f6 * 0.3f + f7 * 0.59f + f8 * 0.11f) * 0.6f;
            f2 = 1.0f - f9 * 0.95f;
            f6 = f6 * f2 + f3 * (1.0f - f2);
            f7 = f7 * f2 + f3 * (1.0f - f2);
            f8 = f8 * f2 + f3 * (1.0f - f2);
        }
        f6 *= f5 * 0.9f + 0.1f;
        f7 *= f5 * 0.9f + 0.1f;
        f8 *= f5 * 0.85f + 0.15f;
        f3 = this.getThunderLevel(partialTick);
        if (f3 > 0.0f) {
            f2 = (f6 * 0.3f + f7 * 0.59f + f8 * 0.11f) * 0.2f;
            float f10 = 1.0f - f3 * 0.95f;
            f6 = f6 * f10 + f2 * (1.0f - f10);
            f7 = f7 * f10 + f2 * (1.0f - f10);
            f8 = f8 * f10 + f2 * (1.0f - f10);
        }
        return Vec3.newTemp(f6, f7, f8);
    }

    public Vec3 getFogColor(float partialTick, int y) {
        float timeOfDay = this.getTimeOfDay(partialTick, y);
        return this.levelSource.getDimension(y).getFogColor(timeOfDay, partialTick);
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getBiomeProvider()Lnet/minecraft/world/level/levelgen/BiomeProvider;"))
    private BiomeProvider getDimensionSkyColor(Level instance, Entity entity) {
        return getBiomeProvider(Mth.floor(entity.y) >> 4);
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getTimeOfDay(F)F"))
    private float getDimensionTime(Level instance, float partialTick, Entity entity) {
        return getTimeOfDay(partialTick, Mth.floor(entity.y) >> 4);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_1426(LightLayer lightType, int x0, int y0, int z0, int x1, int y1, int z1, boolean bl) {
            if (!this.dimension.hasCeiling || lightType != LightLayer.SKY) {
                ++field_310;

                try {
                    if (field_310 != 50) {
                        int chunkX = (x1 + x0) / 2;
                        int chunkY = (y1 + y0) / 2;
                        int chunkZ = (z1 + z0) / 2;
                        if (this.isLoaded(chunkX, chunkY, chunkZ)) {
                            if (!this.getChunkFromPos(chunkX, chunkY, chunkZ).isEmptyChunk()) {
                                int size = this.cubic$lightingUpdates.size();
                                if (bl) {
                                    int var12 = 5;
                                    if (var12 > size) {
                                        var12 = size;
                                    }

                                    synchronized (this.cubic$lightingUpdates) {
                                        for (int var13 = 0; var13 < var12; ++var13) {
                                            LightUpdater lightUpdater = this.cubic$lightingUpdates.get(this.cubic$lightingUpdates.size() - var13 - 1);
                                            if (lightUpdater.type == lightType && lightUpdater.method_1504(x0, y0, z0, x1, y1, z1)) {
                                                return;
                                            }
                                        }
                                    }
                                }

                                this.cubic$lightingUpdates.add(new LightUpdater(lightType, x0, y0, z0, x1, y1, z1));
                                int maxUpdates = 1000000;
                                if (this.cubic$lightingUpdates.size() > maxUpdates) {
                                    System.out.println("More than " + maxUpdates + " updates, aborting lighting updates: " + lightType);
                                    this.cubic$lightingUpdates.clear();
                                }
                            }
                        }
                    }
                } finally {
                    --field_310;
                }
            }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int method_2173(int x, int y, int z) {
        return this.getChunk(x >> 4, y >> 4, z >> 4).getLightLevel(x & 15, y & 15, z & 15, 0);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getLightLevel(int x, int y, int z, boolean bl) {
        if (bl) {
            int var5 = this.getTile(x, y, z);
            if (var5 == Tile.SLAB.id || var5 == Tile.FARMLAND.id || var5 == Tile.COBBLESTONE_STAIRS.id || var5 == Tile.WOOD_STAIRS.id) {
                int var6 = this.getLightLevel(x, y + 1, z, false);
                int var7 = this.getLightLevel(x + 1, y, z, false);
                int var8 = this.getLightLevel(x - 1, y, z, false);
                int var9 = this.getLightLevel(x, y, z + 1, false);
                int var10 = this.getLightLevel(x, y, z - 1, false);
                if (var7 > var6) {
                    var6 = var7;
                }

                if (var8 > var6) {
                    var6 = var8;
                }

                if (var9 > var6) {
                    var6 = var9;
                }

                if (var10 > var6) {
                    var6 = var10;
                }

                return var6;
            }
        }


        x &= 15;
        y &= 15;
        z &= 15;
        return getChunk(x >> 4, y >> 4, z >> 4).getLightLevel(x, y, z, this.skyDarken);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        minX >>= 4;
        minY >>= 4;
        minZ >>= 4;
        maxX >>= 4;
        maxY >>= 4;
        maxZ >>= 4;

        for (int chunkX = minX; chunkX <= maxX; ++chunkX) {
            for (int chunkY = minY; chunkY <= maxY; ++chunkY) {
                for (int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ) {
                    if (!this.hasChunk(chunkX, chunkY, chunkZ)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getBrightness(LightLayer lightType, int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkY = y >> 4;
        int chunkZ = z >> 4;
        if (!this.hasChunk(chunkX, chunkY, chunkZ)) {
            return 0;
        } else {
            return getChunk(chunkX, chunkY, chunkZ).getLightLevel(lightType, x & 15, y & 15, z & 15);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean addEntity(Entity entity) {
        int chunkX = Mth.floor(entity.x / 16.0);
        int chunkY = Mth.floor(entity.y / 16.0);
        int chunkZ = Mth.floor(entity.z / 16.0);
        boolean isPlayer = false;

        if (!isPlayer && !this.hasChunk(chunkX, chunkY, chunkZ)) {
            return false;
        } else {
            if (entity instanceof Player player) {
                this.players.add(player);
                this.updateSleepingPlayerList();
            }

            this.getChunk(chunkX, chunkY, chunkZ).addEntity(entity);
            this.entities.add(entity);
            this.method_272(entity);
            return true;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_304() {
        this.field_283.clear();

        for (Player player : this.players) {
            int xPos = Mth.floor(player.x / 16.0);
            int yPos = Mth.floor(player.y / 16.0);
            int zPos = Mth.floor(player.z / 16.0);
            byte range = 9;

            for (int chunkX = -range; chunkX <= range; ++chunkX) {
                for (int chunkY = -range; chunkY <= range; ++chunkY) {
                    for (int chunkZ = -range; chunkZ <= range; ++chunkZ) {
                        this.field_283.add(new CubicChunkPos(chunkX + xPos, chunkY + yPos, chunkZ + zPos));
                    }
                }
            }
        }

        if (this.field_284 > 0) {
            --this.field_284;
        }

        for (CubicChunkPos pos : this.field_283) {
            int relX = pos.x() * 16;
            int relY = pos.y() * 16;
            int relZ = pos.z() * 16;
            Chunk chunk = this.getChunk(pos.x(), pos.y(), pos.z());
            if (this.field_284 == 0) {
                this.field_292 = this.field_292 * 3 + 1013904223;
                int var17 = this.field_292 >> 2;
                int tileX = var17 & 15;
                int tileZ = var17 >> 8 & 15;
                int tileY = var17 >> 16 & 15;
                int var10 = chunk.getTile(tileX, tileY, tileZ);
                tileX += relX;
                tileZ += relZ;
                if (var10 == 0 && this.method_2173(tileX, tileY, tileZ) <= this.random.nextInt(8) && this.getBrightness(LightLayer.SKY, tileX, tileY, tileZ) <= 0) {
                    Player player = this.getNearestPlayer((double) tileX + 0.5, (double) tileY + 0.5, (double) tileZ + 0.5, 8.0);
                    if (player != null && player.distanceToSqr((double) tileX + 0.5, (double) tileY + 0.5, (double) tileZ + 0.5) > 4.0) {
                        this.playSound((double) tileX + 0.5, (double) tileY + 0.5, (double) tileZ + 0.5, "ambient.cave.cave", 0.7F, 0.8F + this.random.nextFloat() * 0.2F);
                        this.field_284 = this.random.nextInt(12000) + 6000;
                    }
                }
            }

            if (this.random.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
                this.field_292 = this.field_292 * 3 + 1013904223;
                int var18 = this.field_292 >> 2;
                int x = relX + (var18 & 15);
                int z = relZ + (var18 >> 8 & 15);
                int y = this.cubic_getTopY(x, relY, z);
                if (this.isRainingAt(x, y, z)) {
                    this.addGlobalEntity(new LightingBolt((Level) (Object) this, x, y, z));
                    this.field_3013 = 2;
                }
            }

            if (this.random.nextInt(16) == 0) {
                this.field_292 = this.field_292 * 3 + 1013904223;
                int random = this.field_292 >> 2;
                int randomRegion = random & 15;
                int var28 = random >> 8 & 15;
                int var31 = this.cubic_getTopY(randomRegion + relX, relY, var28 + relZ);
                if (this.getBiomeProvider(pos.y()).getBiome(randomRegion + relX, var28 + relZ).hasPrecipitation()
                        && var31 >= 0
                        && var31 < 128
                        && chunk.getLightLevel(LightLayer.BLOCK, randomRegion, var31, var28) < 10) {
                    int var33 = chunk.getTile(randomRegion, var31 - 1, var28);
                    int var35 = chunk.getTile(randomRegion, var31, var28);
                    if (this.isRaining()
                            && var35 == 0
                            && Tile.SNOW_LAYER.method_1120((Level) (Object) this, randomRegion + relX, var31, var28 + relZ)
                            && var33 != 0
                            && var33 != Tile.ICE.id
                            && Tile.tiles[var33].material.method_651()) {
                        this.setTileWithUpdate(randomRegion + relX, var31, var28 + relZ, Tile.SNOW_LAYER.id);
                    }

                    if (var33 == Tile.WATER.id && chunk.getMeta(randomRegion, var31 - 1, var28) == 0) {
                        this.setTileWithUpdate(randomRegion + relX, var31 - 1, var28 + relZ, Tile.ICE.id);
                    }
                }
            }

            for (int var20 = 0; var20 < 80; ++var20) {
                this.field_292 = this.field_292 * 3 + 1013904223;
                int var25 = this.field_292 >> 2;
                int tileX = var25 & 15;
                int tileZ = var25 >> 8 & 15;
                int tileY = var25 >> 16 & 15;
                int tile = chunk.tiles[CubicChunk.getIndex(tileX, tileY, tileZ)] & 255;
                if (Tile.TICKS_RANDOMLY[tile]) {
                    Tile.tiles[tile].randomTick((Level) (Object) this, tileX + relX, tileY, tileZ + relZ, this.random);
                }
            }
        }
    }

    public int cubic_getTopY(int x, int y, int z) {
        Chunk chunk = this.getChunkFromPos(x, y, z);
        x &= 15;
        y &= 15;
        z &= 15;

        int tile = chunk.getTile(x, y, z);
        Material material = tile == 0 ? Material.AIR : Tile.tiles[tile].material;
        if (material.method_651() || material.liquid()) {
            return y + 1;
        }

        return -1;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getTopY(int x, int z) {
        int oldX = x;
        int oldZ = z;
        x &= 15;
        z &= 15;

        for (int y = 127; y > 0; --y) {
            Chunk chunk = this.getChunkFromPos(oldX, y, oldZ);
            int tile = chunk.getTile(x, y & 15, z);
            Material material = tile == 0 ? Material.AIR : Tile.tiles[tile].material;
            if (material.method_651() || material.liquid()) {
                return y + 1;
            }
        }

        return -1;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public TileEntity getTileEntity(int x, int y, int z) {
        Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
        return chunk != null ? chunk.getTileEntity(x & 15, y & 15, z & 15) : null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void placeTileEntity(int x, int y, int z, TileEntity tileEntity) {
        if (!tileEntity.isRemoved()) {
            if (this.field_3455) {
                tileEntity.x = x;
                tileEntity.y = y;
                tileEntity.z = z;
                this.field_3454.add(tileEntity);
            } else {
                this.field_3456.add(tileEntity);
                Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
                if (chunk != null) {
                    chunk.placeTileEntity(x & 15, y & 15, z & 15, tileEntity);
                }
            }
        }
    }

//    @Redirect(method = "getTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getChunk(II)Lnet/minecraft/world/Chunk;"))
//    private Chunk getCubicTileEntity(Level instance, int x, int z, int i, int y) {
//        return getChunk(x, y >> 4, z);
//    }
//
//    @ModifyArg(method = "getTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Chunk;getTileEntity(III)Lnet/minecraft/world/tile/entity/TileEntity;"), index = 1)
//    private int getCubicTileEntity(int y) {
//        return y & 15;
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_2294(int x, int y, int z) {
        TileEntity var4 = this.getTileEntity(x, y, z);
        if (var4 != null && this.field_3455) {
            var4.setRemoved();
        } else {
            if (var4 != null) {
                this.field_3456.remove(var4);
            }

            Chunk var5 = this.getChunk(x >> 4, y >> 4, z >> 4);
            if (var5 != null) {
                var5.removeTileEntity(x & 15, y & 15, z & 15);
            }
        }
    }

//    @Redirect(method = "placeTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getChunk(II)Lnet/minecraft/world/Chunk;"))
//    private Chunk useCubic(Level instance, int x, int z, int i, int y) {
//        return getChunk(x, y >> 4, z);
//    }
//
//    @ModifyArg(method = "placeTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Chunk;placeTileEntity(IIILnet/minecraft/world/tile/entity/TileEntity;)V"), index = 1)
//    private int useCubicChunkY(int y) {
//        return y & 15;
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean setTile(int x, int y, int z, int tile, int meta) {
        Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
        return chunk.setTile(x & 15, y & 15, z & 15, tile, meta);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean setTile(int x, int y, int z, int tile) {
        Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
        return chunk.setTile(x & 15, y & 15, z & 15, tile);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean setMeta(int x, int y, int z, int meta) {
        Chunk chunk = this.getChunk(x >> 4, y >> 4, z >> 4);
        x &= 15;
        y &= 15;
        z &= 15;
        chunk.setMeta(x, y, z, meta);
        return true;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_286() {
        for (int var1 = 0; var1 < this.globalEntities.size(); ++var1) {
            Entity var2 = this.globalEntities.get(var1);
            var2.tick();
            if (var2.removed) {
                this.globalEntities.remove(var1--);
            }
        }

        this.entities.removeAll(this.field_274);

        for (int var5 = 0; var5 < this.field_274.size(); ++var5) {
            Entity entity = this.field_274.get(var5);
            int chunkX = entity.xChunk;
            int chunkY = entity.yChunk;
            int chunkZ = entity.zChunk;
            if (entity.inChunk && this.hasChunk(chunkX, chunkY, chunkZ)) {
                this.getChunk(chunkX, chunkY, chunkZ).removeEntity(entity);
            }
        }

        for (int var6 = 0; var6 < this.field_274.size(); ++var6) {
            this.method_279(this.field_274.get(var6));
        }

        this.field_274.clear();

        for (int var7 = 0; var7 < this.entities.size(); ++var7) {
            Entity entity = this.entities.get(var7);
            if (entity.vechicle != null) {
                if (!entity.vechicle.removed && entity.vechicle.passenger == entity) {
                    continue;
                }

                entity.vechicle.passenger = null;
                entity.vechicle = null;
            }

            if (!entity.removed) {
                this.method_290(entity);
            }

            if (entity.removed) {
                int chunkX = entity.xChunk;
                int chunkY = entity.yChunk;
                int chunkZ = entity.zChunk;
                if (entity.inChunk && this.hasChunk(chunkX, chunkY, chunkZ)) {
                    this.getChunk(chunkX, chunkY, chunkZ).removeEntity(entity);
                }

                this.entities.remove(var7--);
                this.method_279(entity);
            }
        }

        this.field_3455 = true;
        Iterator var8 = this.field_3456.iterator();

        while (var8.hasNext()) {
            TileEntity var11 = (TileEntity) var8.next();
            if (!var11.isRemoved()) {
                var11.tick();
            }

            if (var11.isRemoved()) {
                var8.remove();
                Chunk var14 = this.getChunk(var11.x >> 4, var11.y >> 4, var11.z >> 4);
                if (var14 != null) {
                    var14.removeTileEntity(var11.x & 15, var11.y & 15, var11.z & 15);
                }
            }
        }

        this.field_3455 = false;
        if (!this.field_3454.isEmpty()) {
            for (TileEntity var15 : this.field_3454) {
                if (!var15.isRemoved()) {
                    if (!this.field_3456.contains(var15)) {
                        this.field_3456.add(var15);
                    }

                    Chunk var17 = this.getChunk(var15.x >> 4, var15.y >> 4, var15.z >> 4);
                    if (var17 != null) {
                        var17.placeTileEntity(var15.x & 15, var15.y & 15, var15.z & 15, var15);
                    }

                    this.method_299(var15.x, var15.y, var15.z);
                }
            }

            this.field_3454.clear();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_253(Entity entity, boolean bl) {
        int x = Mth.floor(entity.x);
        int y = Mth.floor(entity.y);
        int z = Mth.floor(entity.z);
        byte range = 32;
        if (!bl || this.isRegionLoaded(x - range, y - range, z - range, x + range, y + range, z + range)) {
            entity.xOld = entity.x;
            entity.yOld = entity.y;
            entity.zOld = entity.z;
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            if (bl && entity.inChunk) {
                if (entity.vechicle != null) {
                    entity.rideTick();
                } else {
                    entity.tick();
                }
            }

            if (Double.isNaN(entity.x) || Double.isInfinite(entity.x)) {
                entity.x = entity.xOld;
            }

            if (Double.isNaN(entity.y) || Double.isInfinite(entity.y)) {
                entity.y = entity.yOld;
            }

            if (Double.isNaN(entity.z) || Double.isInfinite(entity.z)) {
                entity.z = entity.zOld;
            }

            if (Double.isNaN((double) entity.xRot) || Double.isInfinite((double) entity.xRot)) {
                entity.xRot = entity.xRotO;
            }

            if (Double.isNaN((double) entity.yRot) || Double.isInfinite((double) entity.yRot)) {
                entity.yRot = entity.yRotO;
            }

            int chunkX = Mth.floor(entity.x / 16.0);
            int chunkY = Mth.floor(entity.y / 16.0);
            int chunkZ = Mth.floor(entity.z / 16.0);
            if (!entity.inChunk || entity.xChunk != chunkX || entity.yChunk != chunkY || entity.zChunk != chunkZ) {
                if (entity.inChunk && this.hasChunk(entity.xChunk, entity.yChunk, entity.zChunk)) {
                    this.getChunk(entity.xChunk, entity.yChunk, entity.zChunk).removeEntity(entity, entity.yChunk);
                }

                if (this.hasChunk(chunkX, chunkY, chunkZ)) {
                    entity.inChunk = true;
                    this.getChunk(chunkX, chunkY, chunkZ).addEntity(entity);
                } else {
                    entity.inChunk = false;
                }
            }

            if (bl && entity.inChunk && entity.passenger != null) {
                if (!entity.passenger.removed && entity.passenger.vechicle == entity) {
                    this.method_290(entity.passenger);
                } else {
                    entity.passenger.vechicle = null;
                    entity.passenger = null;
                }
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void loadChunksAroundEntity(Entity entity) {
        int centerX = Mth.floor(entity.x / 16.0);
        int centerY = Mth.floor(entity.y / 16.0);
        int centerZ = Mth.floor(entity.z / 16.0);
        byte range = 2;

        for (int chunkX = centerX - range; chunkX <= centerX + range; ++chunkX) {
            for (int chunkY = centerY - range; chunkY <= centerY + range; ++chunkY) {
                for (int chunkZ = centerZ - range; chunkZ <= centerZ + range; ++chunkZ) {
                    this.getChunk(chunkX, chunkY, chunkZ);
                }
            }
        }

        if (!this.entities.contains(entity)) {
            this.entities.add(entity);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean method_302(int x, int y, int z) {
        return this.getChunk(x >> 4, y >> 4, z >> 4).isHighestTile(x & 15, y, z & 15);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setInitialSpawn() {
        this.field_2818 = true;
        int var1 = 0;
        byte var2 = 64;

        int var3 = 0;
        for (var3 = 0; !this.dimension.isValidSpawnPosition(var1, var3); var3 += this.random.nextInt(64) - this.random.nextInt(64)) {
            var1 += this.random.nextInt(64) - this.random.nextInt(64);
        }

        this.levelData.setSpawnXYZ(var1, var2, var3);
        this.field_2818 = false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public List<AABB> getEntityCollisions(Entity entity, AABB aABB) {
        this.field_281.clear();
        int minX = Mth.floor(aABB.x0);
        int maxX = Mth.floor(aABB.x1 + 1.0);
        int minY = Mth.floor(aABB.y0);
        int maxY = Mth.floor(aABB.y1 + 1.0);
        int minZ = Mth.floor(aABB.z0);
        int maxZ = Mth.floor(aABB.z1 + 1.0);

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    if (this.isLoaded(x, y, z)) {
                        Tile var12 = Tile.tiles[this.getTile(x, y, z)];
                        if (var12 != null) {
                            var12.method_1124((Level) (Object) this, x, y, z, aABB, this.field_281);
                        }
                    }
                }
            }
        }

        double var14 = 0.25;
        List<Entity> var15 = this.getEntities(entity, aABB.inflate(var14, var14, var14));

        for (int var16 = 0; var16 < var15.size(); ++var16) {
            AABB var13 = var15.get(var16).getCollideBox();
            if (var13 != null && var13.intersects(aABB)) {
                this.field_281.add(var13);
            }

            var13 = entity.getCollideAgainstBox(var15.get(var16));
            if (var13 != null && var13.intersects(aABB)) {
                this.field_281.add(var13);
            }
        }

        return this.field_281;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isHighestTile(int x, int y, int z) {
        if (!this.hasChunk(x >> 4, y >> 4, z >> 4)) {
            return false;
        } else {
            Chunk var4 = this.getChunk(x >> 4, y >> 4, z >> 4);
            x &= 15;
//            y &= 15;
            z &= 15;
            return var4.isHighestTile(x, y, z);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setLightLevel(LightLayer lightType, int x, int y, int z, int level) {
        if (this.hasChunk(x >> 4, y >> 4, z >> 4)) {
            getChunk(x >> 4, y >> 4, z >> 4).setLightLevel(lightType, x & 15, y & 15, z & 15, level);

            for (int var7 = 0; var7 < this.listeners.size(); ++var7) {
                this.listeners.get(var7).tileChanged(x, y, z);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void method_218(int x, int y, int z, int xOff, int yOff, int zOff, byte[] bs) {
        int minX = x >> 4;
        int minY = y >> 4;
        int minZ = z >> 4;
        int maxX = x + xOff - 1 >> 4;
        int maxY = y + yOff - 1 >> 4;
        int maxZ = z + zOff - 1 >> 4;
        int var12 = 0;

        for (int chunkX = minX; chunkX <= maxX; ++chunkX) {
            int var16 = x - chunkX * 16;
            int var17 = x + xOff - chunkX * 16;
            if (var16 < 0) {
                var16 = 0;
            }

            if (var17 > 16) {
                var17 = 16;
            }

            for (int chunkY = minY; chunkY <= maxY; ++chunkY) {
                int rvar16 = y - chunkY * 16;
                int rvar17 = y + yOff - chunkX * 16;
                if (rvar16 < 0) {
                    rvar16 = 0;
                }

                if (rvar17 > 16) {
                    rvar17 = 16;
                }

                for (int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ) {
                    int var19 = z - chunkZ * 16;
                    int var20 = z + zOff - chunkZ * 16;
                    if (var19 < 0) {
                        var19 = 0;
                    }

                    if (var20 > 16) {
                        var20 = 16;
                    }

                    var12 = this.getChunk(chunkX, chunkY, chunkZ).method_631(bs, var16, rvar16, var19, var17, rvar17, var20, var12);
                    this.method_263(chunkX * 16 + var16, chunkY * 16 + rvar16, chunkZ * 16 + var19, chunkX * 16 + var17, chunkY * 16 + rvar17, chunkZ * 16 + var20);
                }
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void removeEntities() {
        this.entities.removeAll(this.field_274);

        for (int var1 = 0; var1 < this.field_274.size(); ++var1) {
            Entity entity = this.field_274.get(var1);
            int chunkX = entity.xChunk;
            int chunkY = entity.yChunk;
            int chunkZ = entity.zChunk;
            if (entity.inChunk && this.hasChunk(chunkX, chunkY, chunkZ)) {
                this.getChunk(chunkX, chunkY, chunkZ).removeEntity(entity);
            }
        }

        for (int var5 = 0; var5 < this.field_274.size(); ++var5) {
            this.method_279(this.field_274.get(var5));
        }

        this.field_274.clear();

        for (int var6 = 0; var6 < this.entities.size(); ++var6) {
            Entity entity = this.entities.get(var6);
            if (entity.vechicle != null) {
                if (!entity.vechicle.removed && entity.vechicle.passenger == entity) {
                    continue;
                }

                entity.vechicle.passenger = null;
                entity.vechicle = null;
            }

            if (entity.removed) {
                int chunkX = entity.xChunk;
                int chunkY = entity.yChunk;
                int chunkZ = entity.zChunk;
                if (entity.inChunk && this.hasChunk(chunkX, chunkY, chunkZ)) {
                    this.getChunk(chunkX, chunkY, chunkZ).removeEntity(entity);
                }

                this.entities.remove(var6--);
                this.method_279(entity);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean method_295() {
        if (this.field_282 >= 50) {
            return false;
        } else {
            ++this.field_282;

            try {


                synchronized (this.cubic$lightingUpdates) {
                    int var1 = 500;
                    while (this.cubic$lightingUpdates.size() > 0) {
                        if (--var1 <= 0) {
                            return true;
                        }

                        this.cubic$lightingUpdates.remove(this.cubic$lightingUpdates.size() - 1).update((Level) (Object) this);
                    }
                }

                return false;
            } finally {
                --this.field_282;
            }
        }
    }
}
