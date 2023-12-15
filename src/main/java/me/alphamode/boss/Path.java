package me.alphamode.boss;

import me.alphamode.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Set;

public class Path {
    private final List<Node> nodes;
    private Node[] openSet = new Node[0];
    private Node[] closedSet = new Node[0];
//    @Environment(EnvType.CLIENT)
    private Set<Target> targetNodes;
    private int index;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> list, BlockPos blockPos, boolean bl) {
        this.nodes = list;
        this.target = blockPos;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : ((Node)this.nodes.get(this.nodes.size() - 1)).distanceManhattan(this.target);
        this.reached = bl;
    }

    public void next() {
        ++this.index;
    }

    public boolean isDone() {
        return this.index >= this.nodes.size();
    }

    @Nullable
    public Node last() {
        return !this.nodes.isEmpty() ? (Node)this.nodes.get(this.nodes.size() - 1) : null;
    }

    public Node get(int i) {
        return (Node)this.nodes.get(i);
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public void truncate(int i) {
        if (this.nodes.size() > i) {
            this.nodes.subList(i, this.nodes.size()).clear();
        }
    }

    public void set(int i, Node node) {
        this.nodes.set(i, node);
    }

    public int getSize() {
        return this.nodes.size();
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public Vec3 getPos(Entity entity, int i) {
        Node node = (Node)this.nodes.get(i);
        double d = (double)node.x + (double)((int)(entity.dimensionsWidth + 1.0F)) * 0.5;
        double e = (double)node.y;
        double f = (double)node.z + (double)((int)(entity.dimensionsWidth + 1.0F)) * 0.5;
        return new Vec3(d, e, f);
    }

    public Vec3 currentPos(Entity entity) {
        return this.getPos(entity, this.index);
    }

    public Vec3 currentPos() {
        Node node = (Node)this.nodes.get(this.index);
        return new Vec3((double)node.x, (double)node.y, (double)node.z);
    }

    public boolean sameAs(@Nullable Path path) {
        if (path == null) {
            return false;
        } else if (path.nodes.size() != this.nodes.size()) {
            return false;
        } else {
            for(int i = 0; i < this.nodes.size(); ++i) {
                Node node = (Node)this.nodes.get(i);
                Node node2 = (Node)path.nodes.get(i);
                if (node.x != node2.x || node.y != node2.y || node.z != node2.z) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean canReach() {
        return this.reached;
    }

//    @Environment(EnvType.CLIENT)
    public Node[] getOpenSet() {
        return this.openSet;
    }

//    @Environment(EnvType.CLIENT)
    public Node[] getClosedSet() {
        return this.closedSet;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }
}