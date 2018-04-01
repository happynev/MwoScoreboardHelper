package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class ImageModifier {

    private BufferedImage self;

    public ImageModifier(BufferedImage self) {
        this.self = self;
    }

    private static BufferedImage extractSpecificColor(BufferedImage input, int[] min, int[] max) {
        byte[] matchMin = new byte[]{(byte) min[0], (byte) min[1], (byte) min[2]};
        byte[] matchMax = new byte[]{(byte) max[0], (byte) max[1], (byte) max[2]};
        int white = buildColorInt(new byte[]{(byte) 255, (byte) 255, (byte) 255});
        int black = buildColorInt(new byte[]{(byte) 0, (byte) 0, (byte) 0});
        BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (input.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB || input.getRaster().getTransferType() != DataBuffer.TYPE_INT) {
            input = convertToRGB(input);
        }
        int len = input.getWidth() * input.getHeight();
        int[] buffer = new int[len];
        input.getRaster().getDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        for (int i = 0; i < len; i++) {
            byte[] src = splitColorInt(buffer[i]);
            if (checkBetween(src, matchMin, matchMax)) {
                //buffer[i] = black;
            } else {
                buffer[i] = black;
            }
        }

        out.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        return out;
    }

    private static BufferedImage convertToRGB(BufferedImage src) {
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return img;
    }

    private static BufferedImage threshold(BufferedImage input, int[] min) {
        byte[] matchMin = new byte[]{(byte) min[0], (byte) min[1], (byte) min[2]};
        byte[] black = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (input.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB || input.getRaster().getTransferType() != DataBuffer.TYPE_INT) {
            input = convertToRGB(input);
        }
        int len = input.getWidth() * input.getHeight();
        int[] buffer = new int[len];
        input.getRaster().getDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        for (int i = 0; i < len; i++) {
            byte[] src = splitColorInt(buffer[i]);
            if (checkBetween(src, black, matchMin)) {
                buffer[i] = buildColorInt(black);
            }
        }

        out.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        return out;
    }

    private static BufferedImage invert(BufferedImage input) {
        BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (input.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB || input.getRaster().getTransferType() != DataBuffer.TYPE_INT) {
            input = convertToRGB(input);
        }
        int len = input.getWidth() * input.getHeight();
        int[] buffer = new int[len];
        input.getRaster().getDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        for (int i = 0; i < len; i++) {
            byte[] src = splitColorInt(buffer[i]);
            buffer[i] = buildColorInt(invertRGB(src));
        }

        out.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        return out;
    }

    private static float getHueDistance(float[] a, float[] b) {
        return Math.abs(a[0] - b[0]);
    }

    private static boolean checkBetween(byte[] input, byte[] min, byte[] max) {
        //System.out.println("check " + (input[0] & (0xff)) + "/" + (input[1] & (0xff)) + "/" + (input[2] & (0xff)));
        for (int i = 0; i < input.length; i++) {
            //System.out.println(i + " check vs " + (min[i] & (0xff)) + " and " + (max[i] & (0xff)));
            if ((input[i] & (0xff)) < (min[i] & (0xff))) return false;
            if ((input[i] & (0xff)) > (max[i] & (0xff))) return false;
        }
        return true;
    }

    private static boolean checkHueDistance(byte[] a, byte[] b, int max) {
        float fmax = (max) / 255f;
        float[] fa = convertRGBtoHSB(a);
        float[] fb = convertRGBtoHSB(b);
        return getHueDistance(fa, fb) < fmax;
    }

    private static boolean checkRgbDistance(byte[] a, byte[] b, int max) {
        int dr = Math.abs(a[0] - b[0]);
        if (dr >= max) return false;
        int dg = Math.abs(a[1] - b[1]);
        if (dg >= max) return false;
        int db = Math.abs(a[2] - b[2]);
        return db < max;
    }

    private static int getRgbDistance(byte[] a, byte[] b) {
        double dist = 0;
        int ravg = (a[0] + b[0]) / 2;
        int dr = Math.abs(a[0] - b[0]);
        int dg = Math.abs(a[1] - b[1]);
        int db = Math.abs(a[2] - b[2]);
        dist += (512 + ravg) * dr * dr;
        dist += 4 * dg * dg;
        dist += (767 - ravg) * db * db;
        //(((512+rmean)*r*r)>>8)
        // + 4*g*g
        // + (((767-rmean)*b*b)>>8);
        return (int) (Math.sqrt(dist));
    }

    private static float[] convertRGBtoHSB(byte[] in) {
        return Color.RGBtoHSB(in[0], in[1], in[2], null);
    }

    private static int convertHSBtoRGB(float[] in) {
        return Color.HSBtoRGB(in[0], in[1], in[3]);
    }

    private static int buildColorInt(byte[] rgb) {
        return (((int) rgb[2] & 0xFF) << 16) |
                (((int) rgb[1] & 0xFF) << 8) |
                (((int) rgb[0] & 0xFF));
    }

    private static byte[] invertRGB(byte[] rgb) {
        rgb[0] = (byte) (255 - rgb[0]);
        rgb[1] = (byte) (255 - rgb[1]);
        rgb[2] = (byte) (255 - rgb[2]);
        return rgb;
    }

    private static byte[] splitColorInt(int rgb) {
        byte[] ret = new byte[3];
        ret[0] = (byte) (rgb >> 16);
        ret[1] = (byte) (rgb >> 8);
        ret[2] = (byte) (rgb >> 0);
        return ret;
    }

    private static BufferedImage resizeImage(BufferedImage img, double factor) {
        int newHeight = (int) (img.getHeight() * factor);
        int newWidth = (int) (img.getWidth() * factor);
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_DEFAULT);
        BufferedImage sbi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = sbi.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return sbi;
    }

    public BufferedImage getImage() {
        return self;
    }

    public ImageModifier extractSpecificColor(int[] min, int[] max) {
        self = extractSpecificColor(self, min, max);
        return this;
    }

    public ImageModifier extractWhiteOnBlack() {
        //self = extractSpecificColor(self, new int[]{80, 80, 80}, new int[]{255, 255, 255});
        self = threshold(self, new int[]{128, 128, 128});
        return this;
    }

    public ImageModifier extractWhiteNarrow() {
        self = extractSpecificColor(self, new int[]{190, 190, 200}, new int[]{255, 255, 255});
        return this;
    }

    public ImageModifier extractYellow() {
        self = extractSpecificColor(self, new int[]{165, 145, 30}, new int[]{255, 230, 115});
        return this;
    }

    public ImageModifier resizeImage(double factor) {
        self = resizeImage(self, factor);
        return this;
    }

    public ImageModifier invert() {
        self = invert(self);
        return this;
    }

    public ImageModifier upscale() {
        double factor = 50d / (double) self.getHeight(); //for reference: 45px is a playername height on 4k
        if (factor > 1) {
            self = resizeImage(self, factor);
        }
        return this;
    }
}
