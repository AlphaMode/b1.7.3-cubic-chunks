package me.alphamode.tile;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.tile.PortalTile;
import net.minecraft.world.tile.Tile;

public class TheEndTiles {
    public static final Tile END_PORTAL = new PortalTile(119, 111);
    public static final Tile END_PORTAL_FRAME = new Tile(120, 111, Material.field_862);
    public static final Tile END_STONE = new EndstoneTile(121, 111).setTranslationKey("endstone");


    public static void init() {}
}
