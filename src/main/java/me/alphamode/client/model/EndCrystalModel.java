package me.alphamode.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;

//@Environment(EnvType.CLIENT)
public class EndCrystalModel<T extends Entity> extends EntityModel {
    private final ModelPart cube;
    private final ModelPart glass = new ModelPart(0, 0);
    private final ModelPart base;

    public EndCrystalModel(float f, boolean hasBase) {
        this.glass.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        this.cube = new ModelPart(32, 0);
        this.cube.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        if (hasBase) {
            this.base = new ModelPart(0, 16);
            this.base.addBox(-6.0F, 0.0F, -6.0F, 12, 4, 12);
        } else {
            this.base = null;
        }
    }

    @Override
    public void render(float f, float g, float h, float i, float j, float scale) {
        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glTranslatef(0.0F, -0.5F, 0.0F);
        if (this.base != null) {
            this.base.render(scale);
        }

        GL11.glRotatef(g, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, 0.8F + h, 0.0F);
        GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        this.glass.render(scale);
        float l = 0.875F;
        GL11.glScalef(0.875F, 0.875F, 0.875F);
        GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        GL11.glRotatef(g, 0.0F, 1.0F, 0.0F);
        this.glass.render(scale);
        GL11.glScalef(0.875F, 0.875F, 0.875F);
        GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        GL11.glRotatef(g, 0.0F, 1.0F, 0.0F);
        this.cube.render(scale);
        GL11.glPopMatrix();
    }
}
