package me.alphamode.client.renderer;

import me.alphamode.boss.EndCrystal;
import me.alphamode.client.model.EndCrystalModel;
import net.minecraft.EntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;

public class EndCrystalRenderer extends EntityRenderer {
    private final EntityModel model = new EndCrystalModel(0.0F, true);
    private final EntityModel modelWithoutBottom = new EndCrystalModel(0.0F, false);
    @Override
    public void render(Entity entity, double x, double y, double z, float yRot, float a) {
        EndCrystal endCrystal = (EndCrystal) entity;
        float i = (float)endCrystal.time + a;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        this.bindTexture("/end_crystal/end_crystal.png");
        float j = Mth.sin(i * 0.2F) / 2.0F + 0.5F;
        j = j * j + j;

        if (endCrystal.showsBottom()) {
            this.model.render(0.0F, i * 3.0F, j * 0.2F, 0.0F, 0.0F, 0.0625F);
        } else {
            this.modelWithoutBottom.render(0.0F, i * 3.0F, j * 0.2F, 0.0F, 0.0F, 0.0625F);
        }

        GL11.glPopMatrix();
        BlockPos blockPos = endCrystal.getBeamTarget();
        if (blockPos != null) {
//            this.bindTexture(EnderDragonRenderer.CRYSTAL_BEAM_LOCATION);
//            float k = (float)blockPos.getX() + 0.5F;
//            float l = (float)blockPos.getY() + 0.5F;
//            float m = (float)blockPos.getZ() + 0.5F;
//            double n = (double)k - endCrystal.x;
//            double o = (double)l - endCrystal.y;
//            double p = (double)m - endCrystal.z;
//            EnderDragonRenderer.renderCrystalBeams(
//                    d + n, e - 0.3 + (double)(j * 0.4F) + o, f + p, h, (double)k, (double)l, (double)m, endCrystal.time, endCrystal.x, endCrystal.y, endCrystal.z
//            );
        }
    }
}
