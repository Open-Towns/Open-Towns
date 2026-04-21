package xaos.utils;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public final class ColorGL implements Externalizable {

    private static final long serialVersionUID = 2966033656237585470L;

    public static final ColorGL BLACK = new ColorGL(Color.BLACK);
    public static final ColorGL WHITE = new ColorGL(Color.WHITE);
    public static final ColorGL WHITE_DARKER = new ColorGL(Color.WHITE.darker());
    public static final ColorGL GREEN = new ColorGL(Color.GREEN);
    public static final ColorGL RED = new ColorGL(Color.RED);
    public static final ColorGL YELLOW = new ColorGL(Color.YELLOW);
    public static final ColorGL ORANGE = new ColorGL(Color.ORANGE);
    public static final ColorGL GRAY = new ColorGL(Color.GRAY);
    public static final ColorGL LIGHT_GRAY = new ColorGL(Color.LIGHT_GRAY);

    public float r;
    public float g;
    public float b;

    public ColorGL() {
    }

    public ColorGL(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public ColorGL(Color color) {
        if (color != null) {
            r = color.getRed() / 256f;
            g = color.getGreen() / 256f;
            b = color.getBlue() / 256f;
        } else {
            r = 1;
            g = 1;
            b = 1;
        }
    }

    public Color toColor() {
        return new Color(r, g, b);
    }

    /*
     public static ColorGL mergeColor (ColorGL colorSource, float modifier) {
     if (colorSource == null) {
     return new ColorGL (modifier, modifier, modifier);
     } else {
     return new ColorGL (colorSource.r * modifier, colorSource.g * modifier, colorSource.b * modifier);
     }
     }


     public static ColorGL mergeColor (ColorGL colorSource, float modifier, Cell cell) {
     if (colorSource == null) {
     if (cell.isLight ()) {
     float fRed;
     if (cell.isLightRedFull ()) {
     fRed = modifier + 0.2f;
     } else if (cell.isLightRedHalf ()) {
     fRed = modifier + 0.1f;
     } else {
     fRed = modifier - 0.2f;
     }
     if (fRed > 1f) {
     fRed = 1f;
     } else if (fRed < 0) {
     fRed = 0;
     }
     float fGreen;
     if (cell.isLightGreenFull ()) {
     fGreen = modifier + 0.2f;
     } else if (cell.isLightGreenHalf ()) {
     fGreen = modifier + 0.1f;
     } else {
     fGreen = modifier - 0.2f;
     }
     if (fGreen > 1f) {
     fGreen = 1f;
     } else if (fGreen < 0) {
     fGreen = 0;
     }
     float fBlue;
     if (cell.isLightBlueFull ()) {
     fBlue = modifier + 0.2f;
     } else if (cell.isLightBlueHalf ()) {
     fBlue = modifier + 0.1f;
     } else {
     fBlue = modifier - 0.2f;
     }
     if (fBlue > 1f) {
     fBlue = 1f;
     } else if (fBlue < 0) {
     fBlue = 0;
     }
     return new ColorGL (fRed, fGreen, fBlue);
     } else {
     return new ColorGL (modifier, modifier, modifier);
     }
     } else {
     if (cell.isLight ()) {
     float fRed;
     if (cell.isLightRedFull ()) {
     fRed = colorSource.r * modifier + 0.2f;
     } else if (cell.isLightRedHalf ()) {
     fRed = colorSource.r * modifier + 0.1f;
     } else {
     fRed = colorSource.r * modifier - 0.2f;
     }
     if (fRed > 1f) {
     fRed = 1f;
     } else if (fRed < 0) {
     fRed = 0;
     }
     float fGreen;
     if (cell.isLightGreenFull ()) {
     fGreen = colorSource.g * modifier + 0.2f;
     } else if (cell.isLightGreenHalf ()) {
     fGreen = colorSource.g * modifier + 0.1f;
     } else {
     fGreen = colorSource.g * modifier - 0.2f;
     }
     if (fGreen > 1f) {
     fGreen = 1f;
     } else if (fGreen < 0) {
     fGreen = 0;
     }
     float fBlue;
     if (cell.isLightBlueFull ()) {
     fBlue = colorSource.b * modifier + 0.2f;
     } else if (cell.isLightBlueHalf ()) {
     fBlue = colorSource.b * modifier + 0.1f;
     } else {
     fBlue = colorSource.b * modifier - 0.2f;
     }
     if (fBlue > 1f) {
     fBlue = 1f;
     } else if (fBlue < 0) {
     fBlue = 0;
     }
     return new ColorGL (fRed, fGreen, fBlue);
     } else {
     return new ColorGL (colorSource.r * modifier, colorSource.g * modifier, colorSource.b * modifier);
     }
     }
     }
     */
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        r = in.readFloat();
        g = in.readFloat();
        b = in.readFloat();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(r);
        out.writeFloat(g);
        out.writeFloat(b);
    }
}
