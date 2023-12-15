package me.alphamode.mixin;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.Level;
import net.minecraft.world.entity.Player;
import net.minecraft.world.entity.Squid;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Squid.class)
public class SquidMixin extends WaterAnimal {
    public SquidMixin(Level level) {
        super(level);
    }

    @Override
    public boolean interact(Player var1) {
        ItemInstance held = var1.inventory.getSelected();
        if (held != null && held.id == Item.BUCKET.id) {
            var1.inventory.setItem(var1.inventory.selected, new ItemInstance(Item.MILK_BUCKET));
            return true;
        } else {
            return false;
        }
    }
}
