package me.alphamode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

// A simple cache and wrapper for efficiently multiple RegionFiles simultaneously.
public class CubicRegionFileCache {
    private static final Map<File, Reference<CubicRegionFile>> cache = new HashMap<>();

    private CubicRegionFileCache() {
    }

    public static synchronized CubicRegionFile getRegionFile(File basePath, int x, int y, int z) {
        File regionDir = new File(basePath, "region");
        File file = new File(regionDir, "r." + (x >> 5) + "." + (y >> 5) + "." + (z >> 5) + ".mcr");

        Reference<CubicRegionFile> ref = cache.get(file);

        if (ref != null) {
            CubicRegionFile r = ref.get();
            if (r != null) {
                return r;
            }
        }

        if (!regionDir.exists()) {
            regionDir.mkdirs();
        }

        if (cache.size() >= 256) {
            clear();
        }

        CubicRegionFile reg = new CubicRegionFile(file);
        cache.put(file, new SoftReference<>(reg));
        return reg;
    }

    public static synchronized void clear() {
        for(Reference<CubicRegionFile> ref : cache.values()) {
            CubicRegionFile r = ref.get();
            if (r != null) {
                r.close();
            }
        }

        cache.clear();
    }

    public static int getSizeDelta(File file, int x, int y, int z) {
        CubicRegionFile r = getRegionFile(file, x, y, z);
        return r.getSizeDelta();
    }

    public static DataInputStream getChunkDataInputStream(File file, int x, int y, int z) {
        CubicRegionFile r = getRegionFile(file, x, y, z);
        return r.getChunkDataInputStream(x & 31, y & 31, z & 31);
    }

    public static DataOutputStream getChunkDataOutputStream(File file, int x, int y, int z) {
        CubicRegionFile r = getRegionFile(file, x, y, z);
        return r.getChunkDataOutputStream(x & 31, y & 31, z & 31);
    }
}
