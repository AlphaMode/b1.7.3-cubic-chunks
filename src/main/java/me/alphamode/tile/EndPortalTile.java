package me.alphamode.tile;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.tile.TileEntityTile;
import net.minecraft.world.tile.entity.TileEntity;

public class EndPortalTile extends TileEntityTile {
    protected EndPortalTile(int i, Material arg) {
        super(i, arg);
    }

    @Override
    protected TileEntity createTileEntity() {
        return new TheEndPortalTileEntity();
    }
}
