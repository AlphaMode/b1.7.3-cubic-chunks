package me.alphamode.util;

import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

public enum Direction {
    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new BlockPos(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new BlockPos(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new BlockPos(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new BlockPos(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new BlockPos(-1, 0, 0)),
    EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new BlockPos(1, 0, 0));

    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Direction.Axis axis;
    private final Direction.AxisDirection axisDirection;
    private final BlockPos normal;
    private static final Direction[] VALUES = values();
    private static final Map<String, Direction> BY_NAME = Arrays.stream(VALUES)
            .collect(Collectors.toMap(Direction::getName, direction -> direction));
    private static final Direction[] BY_3D_DATA = Arrays.stream(VALUES)
            .sorted(Comparator.comparingInt(direction -> direction.data3d))
            .toArray(i -> new Direction[i]);
    private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES)
            .filter(direction -> direction.getAxis().isHorizontal())
            .sorted(Comparator.comparingInt(direction -> direction.data2d))
            .toArray(i -> new Direction[i]);

    private Direction(int j, int k, int l, String string2, Direction.AxisDirection axisDirection, Direction.Axis axis, BlockPos vec3i) {
        this.data3d = j;
        this.data2d = l;
        this.oppositeIndex = k;
        this.name = string2;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = vec3i;
    }

    private static Direction[] makeDirectionArray(Direction direction, Direction direction2, Direction direction3) {
        return new Direction[]{direction, direction2, direction3, direction3.getOpposite(), direction2.getOpposite(), direction.getOpposite()};
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public Direction.AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public Direction getOpposite() {
        return from3DDataValue(this.oppositeIndex);
    }

//    @Environment(EnvType.CLIENT)
    public Direction getClockWise(Direction.Axis axis) {
        switch(axis) {
            case X:
                if (this != WEST && this != EAST) {
                    return this.getClockWiseX();
                }

                return this;
            case Y:
                if (this != UP && this != DOWN) {
                    return this.getClockWise();
                }

                return this;
            case Z:
                if (this != NORTH && this != SOUTH) {
                    return this.getClockWiseZ();
                }

                return this;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    public Direction getClockWise() {
        switch(this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

//    @Environment(EnvType.CLIENT)
    private Direction getClockWiseX() {
        switch(this) {
            case NORTH:
                return DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
            case SOUTH:
                return UP;
            case UP:
                return NORTH;
            case DOWN:
                return SOUTH;
        }
    }

//    @Environment(EnvType.CLIENT)
    private Direction getClockWiseZ() {
        switch(this) {
            case EAST:
                return DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return UP;
            case UP:
                return EAST;
            case DOWN:
                return WEST;
        }
    }

    public Direction getCounterClockWise() {
        switch(this) {
            case NORTH:
                return WEST;
            case EAST:
                return NORTH;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    public int getStepX() {
        return this.axis == Direction.Axis.X ? this.axisDirection.getStep() : 0;
    }

    public int getStepY() {
        return this.axis == Direction.Axis.Y ? this.axisDirection.getStep() : 0;
    }

    public int getStepZ() {
        return this.axis == Direction.Axis.Z ? this.axisDirection.getStep() : 0;
    }

    public String getName() {
        return this.name;
    }

    public Direction.Axis getAxis() {
        return this.axis;
    }

    @Nullable
//    @Environment(EnvType.CLIENT)
    public static Direction byName(@Nullable String string) {
        return string == null ? null : (Direction)BY_NAME.get(string.toLowerCase(Locale.ROOT));
    }

    public static Direction from3DDataValue(int i) {
        return BY_3D_DATA[Math.abs(i % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int i) {
        return BY_2D_DATA[Math.abs(i % BY_2D_DATA.length)];
    }

    public static Direction fromYRot(double d) {
        return from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Direction.Axis axis, Direction.AxisDirection axisDirection) {
        switch(axis) {
            case X:
                return axisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            case Y:
                return axisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
            case Z:
            default:
                return axisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
        }
    }

    public float toYRot() {
        return (float)((this.data2d & 3) * 90);
    }

    public static Direction getRandomFace(Random random) {
        return values()[random.nextInt(values().length)];
    }

    public static Direction getNearest(double d, double e, double f) {
        return getNearest((float)d, (float)e, (float)f);
    }

    public static Direction getNearest(float f, float g, float h) {
        Direction direction = NORTH;
        float i = Float.MIN_VALUE;

        for(Direction direction2 : VALUES) {
            float j = f * (float)direction2.normal.x + g * (float)direction2.normal.y + h * (float)direction2.normal.z;
            if (j > i) {
                i = j;
                direction = direction2;
            }
        }

        return direction;
    }

    public String toString() {
        return this.name;
    }

    public static Direction get(Direction.AxisDirection axisDirection, Direction.Axis axis) {
        for(Direction direction : values()) {
            if (direction.getAxisDirection() == axisDirection && direction.getAxis() == axis) {
                return direction;
            }
        }

        throw new IllegalArgumentException("No such direction: " + axisDirection + " " + axis);
    }

    public BlockPos getNormal() {
        return this.normal;
    }

    public static enum Axis implements Predicate<Direction> {
        X("x") {
            @Override
            public int choose(int i, int j, int k) {
                return i;
            }

            @Override
            public double choose(double d, double e, double f) {
                return d;
            }
        },
        Y("y") {
            @Override
            public int choose(int i, int j, int k) {
                return j;
            }

            @Override
            public double choose(double d, double e, double f) {
                return e;
            }
        },
        Z("z") {
            @Override
            public int choose(int i, int j, int k) {
                return k;
            }

            @Override
            public double choose(double d, double e, double f) {
                return f;
            }
        };

        private static final Map<String, Direction.Axis> BY_NAME = (Map<String, Direction.Axis>)Arrays.stream(values())
                .collect(Collectors.toMap(Direction.Axis::getName, axis -> axis));
        private final String name;

        private Axis(String string2) {
            this.name = string2;
        }

        @Nullable
//        @Environment(EnvType.CLIENT)
        public static Direction.Axis byName(String string) {
            return (Direction.Axis)BY_NAME.get(string.toLowerCase(Locale.ROOT));
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public String toString() {
            return this.name;
        }

        public static Direction.Axis getRandomAxis(Random random) {
            return values()[random.nextInt(values().length)];
        }

        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis() == this;
        }

        public Direction.Plane getPlane() {
            switch(this) {
                case X:
                case Z:
                    return Direction.Plane.HORIZONTAL;
                case Y:
                    return Direction.Plane.VERTICAL;
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public abstract int choose(int i, int j, int k);

        public abstract double choose(double d, double e, double f);
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int j, String string2) {
            this.step = j;
            this.name = string2;
        }

        public int getStep() {
            return this.step;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
        HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
        VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

        private final Direction[] faces;
        private final Direction.Axis[] axis;

        private Plane(Direction[] directions, Direction.Axis[] axiss) {
            this.faces = directions;
            this.axis = axiss;
        }

        public Direction getRandomDirection(Random random) {
            return this.faces[random.nextInt(this.faces.length)];
        }

        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis().getPlane() == this;
        }

        public Iterator<Direction> iterator() {
            return Iterators.forArray(this.faces);
        }
    }
}