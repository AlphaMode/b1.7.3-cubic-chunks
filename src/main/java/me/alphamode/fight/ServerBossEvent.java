package me.alphamode.fight;

import java.util.*;

import me.alphamode.MathHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Player;
import net.minecraft.world.entity.ServerPlayer;

public class ServerBossEvent {
    private final Set<Player> players = new HashSet<>();
    private final Set<Player> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
    private boolean visible = true;

    private final UUID id;
    protected String name;
    protected float percent;
    protected boolean darkenScreen;
    protected boolean playBossMusic;
    protected boolean createWorldFog;

    public ServerBossEvent(String component) {
        this.id = MathHelper.createInsecureUUID();
        this.name = component;
        this.percent = 1.0F;
    }

    public void setPercent(float f) {
        if (f != this.percent) {
            this.percent = f;
//            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PCT);
        }
    }

    public ServerBossEvent setDarkenScreen(boolean bl) {
        if (bl != this.darkenScreen) {
            this.darkenScreen = bl;
//            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
        }

        return this;
    }

    public ServerBossEvent setPlayBossMusic(boolean bl) {
        if (bl != this.playBossMusic) {
            this.playBossMusic = bl;
//            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
        }

        return this;
    }

    public ServerBossEvent setCreateWorldFog(boolean bl) {
        if (bl != this.createWorldFog) {
            this.createWorldFog = bl;
//            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
        }

        return this;
    }

    public void setName(String component) {
        if (!component.equals(this.name)) {
            this.name = component;
//            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_NAME);
        }
    }

//    private void broadcast(ClientboundBossEventPacket.Operation operation) {
//        if (this.visible) {
//            ClientboundBossEventPacket clientboundBossEventPacket = new ClientboundBossEventPacket(operation, this);
//
//            for(ServerPlayer serverPlayer : this.players) {
//                serverPlayer.connection.send(clientboundBossEventPacket);
//            }
//        }
//    }

    public void addPlayer(Player serverPlayer) {
        if (this.players.add(serverPlayer) && this.visible) {
//            serverPlayer.connection.send(new ClientboundBossEventPacket(ClientboundBossEventPacket.Operation.ADD, this));
        }
    }

    public void removePlayer(Player serverPlayer) {
        if (this.players.remove(serverPlayer) && this.visible) {
//            serverPlayer.connection.send(new ClientboundBossEventPacket(ClientboundBossEventPacket.Operation.REMOVE, this));
        }
    }

    public void removeAllPlayers() {
        if (!this.players.isEmpty()) {
            for(Player serverPlayer : new ArrayList<>(this.players)) {
                this.removePlayer(serverPlayer);
            }
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean bl) {
        if (bl != this.visible) {
            this.visible = bl;

            for(Player serverPlayer : this.players) {
//                serverPlayer.connection
//                        .send(new ClientboundBossEventPacket(bl ? ClientboundBossEventPacket.Operation.ADD : ClientboundBossEventPacket.Operation.REMOVE, this));
            }
        }
    }

    public Collection<Player> getPlayers() {
        return this.unmodifiablePlayers;
    }
}