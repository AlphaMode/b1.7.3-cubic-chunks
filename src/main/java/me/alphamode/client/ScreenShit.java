package me.alphamode.client;

import net.minecraft.class_561;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenShit {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static ByteBuffer buffer;
    private static byte[] pixelData;
    private static int[] imageData;
    private int e;
    private DataOutputStream f;
    private byte[] g;
    private int h;
    private int i;
    private File j;

    public static String method_1540(File file, int i, int j) {
        try {
            File file2;
            File file3 = new File(file, "screenshots");
            file3.mkdir();
            if (buffer == null || buffer.capacity() < i * j) {
                buffer = BufferUtils.createByteBuffer(i * j * 3);
            }
            if (imageData == null || imageData.length < i * j * 3) {
                pixelData = new byte[i * j * 3];
                imageData = new int[i * j];
            }
            GL11.glPixelStorei(3333, 1);
            GL11.glPixelStorei(3317, 1);
            buffer.clear();
            GL11.glReadPixels(0, 0, i, j, 6407, 5121, buffer);
            buffer.clear();
            String string = "" + dateFormat.format(new Date());
            int n = 1;
            while ((file2 = new File(file3, string + (n == 1 ? "" : "_" + n) + ".png")).exists()) {
                ++n;
            }
            buffer.get(pixelData);
            for (int k = 0; k < i; ++k) {
                for (int i2 = 0; i2 < j; ++i2) {
                    int n2;
                    int n3 = k + (j - i2 - 1) * i;
                    int n4 = pixelData[n3 * 3 + 0] & 0xFF;
                    int n5 = pixelData[n3 * 3 + 1] & 0xFF;
                    int n6 = pixelData[n3 * 3 + 2] & 0xFF;
                    ScreenShit.imageData[k + i2 * i] = n2 = 0xFF000000 | n4 << 16 | n5 << 8 | n6;
                }
            }
            BufferedImage bufferedImage = new BufferedImage(i, j, 1);
            bufferedImage.setRGB(0, 0, i, j, imageData, 0, i);
            ImageIO.write(bufferedImage, "png", file2);
            return "Saved screenshot as " + file2.getName();
        } catch (Exception exception) {
            exception.printStackTrace();
            return "Failed to save: " + exception;
        }
    }

    public ScreenShit(File file1, int i2, int i3, int i4) throws IOException {
        this.h = i2;
        this.i = i3;
        this.e = i4;
        File file5 = new File(file1, "screenshots");
        file5.mkdir();
        String string6 = "huge_" + dateFormat.format(new Date());

        for(int i7 = 1; (this.j = new File(file5, string6 + (i7 == 1 ? "" : "_" + i7) + ".tga")).exists(); ++i7) {
        }

        byte[] b8 = new byte[18];
        b8[2] = 2;
        b8[12] = (byte)(i2 % 256);
        b8[13] = (byte)(i2 / 256);
        b8[14] = (byte)(i3 % 256);
        b8[15] = (byte)(i3 / 256);
        b8[16] = 24;
        this.g = new byte[i2 * i4 * 3];
        this.f = new DataOutputStream(new FileOutputStream(this.j));
        this.f.write(b8);
    }

    public void func_21189_a(ByteBuffer byteBuffer1, int i2, int i3, int i4, int i5) throws IOException {
        int i6 = i4;
        int i7 = i5;
        if(i4 > this.h - i2) {
            i6 = this.h - i2;
        }

        if(i5 > this.i - i3) {
            i7 = this.i - i3;
        }

        this.e = i7;

        for(int i8 = 0; i8 < i7; ++i8) {
            byteBuffer1.position((i5 - i7) * i4 * 3 + i8 * i4 * 3);
            int i9 = (i2 + i8 * this.h) * 3;
            byteBuffer1.get(this.g, i9, i6 * 3);
        }

    }

    public void func_21191_a() throws IOException {
        this.f.write(this.g, 0, this.h * 3 * this.e);
    }

    public String func_21190_b() throws IOException {
        this.f.close();
        return "Saved screenshot as " + this.j.getName();
    }
}
