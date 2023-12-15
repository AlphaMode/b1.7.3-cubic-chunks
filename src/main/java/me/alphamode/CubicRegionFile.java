package me.alphamode;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/*

Region File Format

Concept: The minimum unit of storage on hard drives is 4KB. 90% of Minecraft
 chunks are smaller than 4KB. 99% are smaller than 8KB. Write a simple
 container to store chunks in single files in runs of 4KB sectors.
Each region file represents a 32x32 group of chunks. The conversion from
chunk number to region number is floor(coord / 32): a chunk at (30, -3)
would be in region (0, -1), and one at (70, -30) would be at (3, -1).
Region files are named "r.x.z.data", where x and z are the region coordinates.
A region file begins with a 4KB header that describes where chunks are stored
in the file. A 4-byte big-endian integer represents sector offsets and sector
counts. The chunk offset for a chunk (x, z) begins at byte 4*(x+z*32) in the
file. The bottom byte of the chunk offset indicates the number of sectors the
chunk takes up, and the top 3 bytes represent the sector number of the chunk.
Given a chunk offset o, the chunk data begins at byte 8192*(o/256) and takes up
at most 8192*(o%256) bytes. A chunk cannot exceed 1MB in size. If a chunk
offset is 0, the corresponding chunk is not stored in the region file.
Chunk data begins with a 4-byte big-endian integer representing the chunk data
length in bytes, not counting the length field. The length must be smaller than
8192 times the number of sectors. The next byte is a version field, to allow
backwards-compatible updates to how chunks are encoded.
A version of 1 represents a gzipped NBT file. The gzipped data is the chunk
length - 1.
A version of 2 represents a deflated (zlib compressed) NBT file. The deflated
data is the chunk length - 1.

*/
public class CubicRegionFile {
    static final int CHUNK_HEADER_SIZE = 5;
    private static final byte[] emptySector = new byte[8192];
    private final File fileName;
    private RandomAccessFile file;
    private final int[] offsets;
    private final int[] time;
    private ArrayList<Boolean> sectorFree;
    private int sizeDelta;
    private long lastModified = 0L;

    public CubicRegionFile(File path) {
        this.offsets = new int[32768];
        this.time = new int[32768];
        this.fileName = path;
        this.debugln("REGION LOAD " + this.fileName);
        this.sizeDelta = 0;

        try {
            if (path.exists())
                this.lastModified = path.lastModified();

            file = new RandomAccessFile(path, "rw");

            if (file.length() < 8192L) {
                /* we need to write the chunk offset table */
                for(int i = 0; i < 32768; ++i)
                    file.writeInt(0);

                /* we need to write the timestamp table */
                for(int i = 0; i < 32768; ++i)
                    file.writeInt(0);

                sizeDelta += 16384;
            }

            if ((file.length() & 8191L) != 0L) {
                /* the file size is not a multiple of 8KB, grow it */
                for(int i = 0; (long)i < (file.length() & 8191L); ++i)
                    file.write(0);
            }

            int nSectors = (int)this.file.length() / 8192;
            sectorFree = new ArrayList<>(nSectors);

            for(int i = 0; i < nSectors; ++i) {
                sectorFree.add(true);
            }

            sectorFree.set(0, false); // chunk offset table
            sectorFree.set(1, false); // timestamp table

            file.seek(0L);
            for(int i = 0; i < 32768; ++i) {
                int offset = file.readInt();
                this.offsets[i] = offset;
                if (offset != 0 && (offset >> 8) + (offset & 0xFF) <= sectorFree.size()) {
                    for(int sectorNum = 0; sectorNum < (offset & 0xFF); ++sectorNum) {
                        sectorFree.set((offset >> 8) + sectorNum, false);
                    }
                }
            }

            for(int i = 0; i < 32768; ++i) {
                int offset = this.file.readInt();
                this.time[i] = offset;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* the modification date of the region file when it was first opened */
    public long lastModified() {
        return lastModified;
    }

    /* gets how much the region file has grown since it was last checked */
    public synchronized int getSizeDelta() {
        int var1 = this.sizeDelta;
        this.sizeDelta = 0;
        return var1;
    }

    // various small debug printing helpers
    private void debug(String in) {
        // System.out.println(in);
    }

    private void debugln(String in) {
        this.debug(in + "\n");
    }

    private void debug(String mode, int x, int y, int z, String in) {
        this.debug("REGION " + mode + " " + this.fileName.getName() + "[" + x + "," + y + "," + z + "] = " + in);
    }

    private void debug(String mode, int x, int y, int z, int count, String in) {
        this.debug("REGION " + mode + " " + this.fileName.getName() + "[" + x + "," + y + "," + z + "] " + count + "B = " + in);
    }

    private void debugln(String mode, int x, int y, int z, String in) {
        this.debug(mode, x, y, z, in + "\n");
    }

    /* gets an (uncompressed) stream representing the chunk data
       returns null if the chunk is not found or an error occurs  */
    public synchronized DataInputStream getChunkDataInputStream(int x, int y, int z) {
        if (outOfBounds(x, y, z)) {
            debugln("READ", x, y, z, "out of bounds");
            return null;
        }

        try {
            int offset = getOffset(x, y, z);
            if (offset == 0) {
                debugln("READ", x, y, z, "miss");
                return null;
            }

            int sectorNumber = offset >> 8;
            int numSectors = offset & 0xFF;

            if (sectorNumber + numSectors > sectorFree.size()) {
                this.debugln("READ", x, y, z, "invalid sector");
                return null;
            }

            this.file.seek(sectorNumber * 8192);
            int length = file.readInt();

            if (length > 8192 * numSectors) {
                this.debugln("READ", x, y, z, "invalid length: " + length + " > 8192 * " + numSectors);
                return null;
            }

            byte version = this.file.readByte();

            if (version == 1) {
                byte[] data = new byte[length - 1];
                file.read(data);
                DataInputStream ret = new DataInputStream(new GZIPInputStream(
                        new ByteArrayInputStream(data)
                ));
                // debug("READ", x, y, z, " = found");
                return ret;
            } else if (version == 2) {
                byte[] data = new byte[length - 1];
                file.read(data);
                DataInputStream ret = new DataInputStream(new InflaterInputStream(
                        new ByteArrayInputStream(data)
                ));
                // debug("READ", x, y, z, " = found");
                return ret;
            }

            debugln("READ", x, y, z, "unknown version " + version);
            return null;
        } catch (IOException e) {
            this.debugln("READ", x, y, z, "exception");
            return null;
        }
    }

    public DataOutputStream getChunkDataOutputStream(int x, int y, int z) {
        return this.outOfBounds(x, y, z) ? null : new DataOutputStream(new DeflaterOutputStream(new CubicChunkBuffer(x, y, z)));
    }

    /* lets chunk writing be multithreaded by not locking the whole file as a
       chunk is serializing -- only writes when serialization is over */
    class CubicChunkBuffer extends ByteArrayOutputStream {
        private int x;
        private int y;
        private int z;

        public CubicChunkBuffer(int x, int y, int z) {
            super(8096); // initialize to 8KB
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void close() {
            CubicRegionFile.this.write(this.x, this.y, this.z, this.buf, this.count);
        }
    }

    /* write a chunk at (x,y,z) with length bytes of data to disk */
    protected synchronized void write(int x, int y, int z, byte[] data, int length) {
        try {
            int offset = this.getOffset(x, y, z);
            int sectorNumber = offset >> 8;
            int sectorsAllocated = offset & 0xFF;
            int sectorsNeeded = (length + CHUNK_HEADER_SIZE) / 8192 + 1;

            if (sectorsNeeded >= 256) // maximum chunk size is 1MB
                return;

            if (sectorNumber != 0 && sectorsAllocated == sectorsNeeded) {
                /* we can simply overwrite the old sectors */
                debug("SAVE", x, y, z, length, "rewrite");
                write(sectorNumber, data, length);
            } else {
                /* we need to allocate new sectors */

                /* mark the sectors previously used for this chunk as free */
                for(int i = 0; i < sectorsAllocated; ++i)
                    sectorFree.set(sectorNumber + i, true);

                int runStart = sectorFree.indexOf(true);
                int runLength = 0;
                if (runStart != -1) {
                    for(int i = runStart; i < sectorFree.size(); ++i) {
                        if (runLength != 0) {
                            if (sectorFree.get(i))
                                ++runLength;
                            else
                                runLength = 0;
                        } else if (sectorFree.get(i)) {
                            runStart = i;
                            runLength = 1;
                        }

                        if (runLength >= sectorsNeeded) {
                            break;
                        }
                    }
                }

                if (runLength >= sectorsNeeded) {
                    /* we found a free space large enough */
                    debug("SAVE", x, y, z, length, "reuse");
                    sectorNumber = runStart;
                    setOffset(x, y, z, runStart << 8 | sectorsNeeded);
                    for(int i = 0; i < sectorsNeeded; ++i)
                        sectorFree.set(sectorNumber + i, false);
                    this.write(sectorNumber, data, length);
                } else {
                    /* no free space large enough found -- we need to grow the file */
                    debug("SAVE", x, y, z, length, "grow");
                    file.seek(file.length());
                    sectorNumber = sectorFree.size();

                    for(int i = 0; i < sectorsNeeded; ++i) {
                        file.write(emptySector);
                        sectorFree.add(false);
                    }
                    sizeDelta += 8192 * sectorsNeeded;

                    write(sectorNumber, data, length);
                    setOffset(x, y, z, (sectorNumber << 8) | sectorsNeeded);
                }
            }

            this.setTime(x, y, z, (int)(System.currentTimeMillis() / 1000L));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* write a chunk data to the region file at specified sector number */
    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        this.debugln(" " + sectorNumber);
        this.file.seek(sectorNumber * 8192);
        this.file.writeInt(length + 1);  // chunk length
        this.file.writeByte(2);     // chunk version number
        this.file.write(data, 0, length); // chunk data
    }

    /* is this an invalid chunk coordinate? */
    private boolean outOfBounds(int x, int y, int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    private int getOffset(int x, int y, int z) {
        return this.offsets[x + y * 32 + z * 32 * 32];
    }

    public boolean hasChunk(int x, int y, int z) {
        return this.getOffset(x, y, z) != 0;
    }

    private void setOffset(int x, int y, int z, int offset) throws IOException {
        offsets[x + y * 32 + z * 32 * 32] = offset;
        file.seek((x + y * 32 + z * 32 * 32) * 4);
        file.writeInt(offset);
    }

    private void setTime(int x, int y, int z, int time) throws IOException {
        this.time[x + y * 32 + z * 32 * 32] = time;
        file.seek(8192 + (x + y * 32 + z * 32 * 32) * 4);
        file.writeInt(time);
    }

    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
