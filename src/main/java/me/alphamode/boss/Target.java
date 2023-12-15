package me.alphamode.boss;

public class Target extends Node {
    private float bestHeuristic = Float.MAX_VALUE;
    private Node bestNode;
    private boolean reached;

    public Target(Node node) {
        super(node.x, node.y, node.z);
    }

//    @Environment(EnvType.CLIENT)
    public Target(int i, int j, int k) {
        super(i, j, k);
    }

    public void updateBest(float f, Node node) {
        if (f < this.bestHeuristic) {
            this.bestHeuristic = f;
            this.bestNode = node;
        }
    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }
}