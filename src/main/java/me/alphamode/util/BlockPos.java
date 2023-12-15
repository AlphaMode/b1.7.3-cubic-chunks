package me.alphamode.util;

import com.google.common.collect.AbstractIterator;
import me.alphamode.MathHelper;
import net.minecraft.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//@Unmodifiable
public class BlockPos extends Vec3i {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public BlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos(double d, double e, double f) {
        super(d, e, f);
    }

    public BlockPos(Entity entity) {
        this(entity.x, entity.y, entity.z);
    }

    public BlockPos(Vec3 vec3) {
        this(vec3.x, vec3.y, vec3.z);
    }

    public BlockPos(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long offset(long l, Direction direction) {
        return offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int i, int j, int k) {
        return asLong(getX(l) + i, getY(l) + j, getZ(l) + k);
    }

    public static int getX(long l) {
        return (int)(l << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long l) {
        return (int)(l << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long l) {
        return (int)(l << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long l) {
        return new BlockPos(getX(l), getY(l), getZ(l));
    }

    public static long asLong(int i, int j, int k) {
        long l = 0L;
        l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
        l |= ((long)j & PACKED_Y_MASK) << 0;
        return l | ((long)k & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long l) {
        return l & -16L;
    }

    public long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    public BlockPos offset(double d, double e, double f) {
        return d == 0.0 && e == 0.0 && f == 0.0 ? this : new BlockPos((double)this.getX() + d, (double)this.getY() + e, (double)this.getZ() + f);
    }

    public BlockPos offset(int i, int j, int k) {
        return i == 0 && j == 0 && k == 0 ? this : new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
    }

    public BlockPos offset(Vec3i vec3i) {
        return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public BlockPos subtract(Vec3i vec3i) {
        return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    public BlockPos above() {
        return this.above(1);
    }

    public BlockPos above(int i) {
        return this.relative(Direction.UP, i);
    }

    public BlockPos below() {
        return this.below(1);
    }

    public BlockPos below(int i) {
        return this.relative(Direction.DOWN, i);
    }

    public BlockPos north() {
        return this.north(1);
    }

    public BlockPos north(int i) {
        return this.relative(Direction.NORTH, i);
    }

    public BlockPos south() {
        return this.south(1);
    }

    public BlockPos south(int i) {
        return this.relative(Direction.SOUTH, i);
    }

    public BlockPos west() {
        return this.west(1);
    }

    public BlockPos west(int i) {
        return this.relative(Direction.WEST, i);
    }

    public BlockPos east() {
        return this.east(1);
    }

    public BlockPos east(int i) {
        return this.relative(Direction.EAST, i);
    }

    public BlockPos relative(Direction direction) {
        return this.relative(direction, 1);
    }

    public BlockPos relative(Direction direction, int i) {
        return i == 0 ? this : new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
    }

    public BlockPos cross(Vec3i vec3i) {
        return new BlockPos(
                this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(),
                this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(),
                this.getX() * vec3i.getY() - this.getY() * vec3i.getX()
        );
    }

    public BlockPos immutable() {
        return this;
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return betweenClosed(
                Math.min(blockPos.getX(), blockPos2.getX()),
                Math.min(blockPos.getY(), blockPos2.getY()),
                Math.min(blockPos.getZ(), blockPos2.getZ()),
                Math.max(blockPos.getX(), blockPos2.getX()),
                Math.max(blockPos.getY(), blockPos2.getY()),
                Math.max(blockPos.getZ(), blockPos2.getZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
        return betweenClosedStream(
                Math.min(blockPos.getX(), blockPos2.getX()),
                Math.min(blockPos.getY(), blockPos2.getY()),
                Math.min(blockPos.getZ(), blockPos2.getZ()),
                Math.max(blockPos.getX(), blockPos2.getX()),
                Math.max(blockPos.getY(), blockPos2.getY()),
                Math.max(blockPos.getZ(), blockPos2.getZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(int i, int j, int k, int l, int m, int n) {
        return StreamSupport.stream(new AbstractSpliterator<BlockPos>((long)((l - i + 1) * (m - j + 1) * (n - k + 1)), 64) {
            final Cursor3D cursor = new Cursor3D(i, j, k, l, m, n);
            final MutableBlockPos nextPos = new MutableBlockPos();

            public boolean tryAdvance(Consumer<? super BlockPos> consumer) {
                if (this.cursor.advance()) {
                    consumer.accept(this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }

    public static Iterable<BlockPos> betweenClosed(int i, int j, int k, int l, int m, int n) {
        return () -> new AbstractIterator<BlockPos>() {
            final Cursor3D cursor = new Cursor3D(i, j, k, l, m, n);
            final MutableBlockPos nextPos = new MutableBlockPos();

            protected BlockPos computeNext() {
                return (BlockPos)(this.cursor.advance() ? this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()) : this.endOfData());
            }
        };
    }

    public static class MutableBlockPos extends BlockPos {
        protected int x;
        protected int y;
        protected int z;

        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(BlockPos blockPos) {
            this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        public MutableBlockPos(int i, int j, int k) {
            super(0, 0, 0);
            this.x = i;
            this.y = j;
            this.z = k;
        }

        public MutableBlockPos(double d, double e, double f) {
            this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        @Override
        public BlockPos offset(double d, double e, double f) {
            return super.offset(d, e, f).immutable();
        }

        @Override
        public BlockPos offset(int i, int j, int k) {
            return super.offset(i, j, k).immutable();
        }

        @Override
        public BlockPos relative(Direction direction, int i) {
            return super.relative(direction, i).immutable();
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getZ() {
            return this.z;
        }

        public MutableBlockPos set(int i, int j, int k) {
            this.x = i;
            this.y = j;
            this.z = k;
            return this;
        }

        public MutableBlockPos set(Entity entity) {
            return this.set(entity.x, entity.y, entity.z);
        }

        public MutableBlockPos set(double d, double e, double f) {
            return this.set(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        public MutableBlockPos set(Vec3i vec3i) {
            return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        }

        public MutableBlockPos set(long l) {
            return this.set(getX(l), getY(l), getZ(l));
        }

        public MutableBlockPos move(Direction direction) {
            return this.move(direction, 1);
        }

        public MutableBlockPos move(Direction direction, int i) {
            return this.set(this.x + direction.getStepX() * i, this.y + direction.getStepY() * i, this.z + direction.getStepZ() * i);
        }

        public MutableBlockPos move(int i, int j, int k) {
            return this.set(this.x + i, this.y + j, this.z + k);
        }

        public void setX(int i) {
            this.x = i;
        }

        public void setY(int i) {
            this.y = i;
        }

        public void setZ(int i) {
            this.z = i;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }
    }
}