/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class SpectrumColor {

    static private double Gamma = 1.00;
    static private double IntensityMax = 255;

    final static private Color[] colors = new Color[755];

    public SpectrumColor() {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = waveLengthToRGB(380 + (Math.cbrt(1 / (double) (i + 1)) * (780 - 380)));
        }
    }

    public Color getColor(int i) {
        if (i < 0) {
            i = 0;
        }
        if (i >= colors.length) {
            i = colors.length - 1;
        }
        return colors[i];
    }

    /**
     * Taken from Earl F. Glynn's web page:
     * <a href="http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm">Spectra
     * Lab Report</a>
     *
     */
    public static Color waveLengthToRGB(double Wavelength) {
        double factor;
        double Red, Green, Blue;

        if ((Wavelength >= 380) && (Wavelength < 440)) {
            Red = -(Wavelength - 440) / (440 - 380);
            Green = 0.0;
            Blue = 1.0;
        } else if ((Wavelength >= 440) && (Wavelength < 490)) {
            Red = 0.0;
            Green = (Wavelength - 440) / (490 - 440);
            Blue = 1.0;
        } else if ((Wavelength >= 490) && (Wavelength < 510)) {
            Red = 0.0;
            Green = 1.0;
            Blue = -(Wavelength - 510) / (510 - 490);
        } else if ((Wavelength >= 510) && (Wavelength < 580)) {
            Red = (Wavelength - 510) / (580 - 510);
            Green = 1.0;
            Blue = 0.0;
        } else if ((Wavelength >= 580) && (Wavelength < 645)) {
            Red = 1.0;
            Green = -(Wavelength - 645) / (645 - 580);
            Blue = 0.0;
        } else if ((Wavelength >= 645) && (Wavelength < 781)) {
            Red = 1.0;
            Green = 0.0;
            Blue = 0.0;
        } else {
            Red = 0.0;
            Green = 0.0;
            Blue = 0.0;
        };

        // Let the intensity fall off near the vision limits GM : only towards the red end
        if ((Wavelength >= 380) && (Wavelength < 420)) {
            factor = 1.0; // 0.3 + 0.7 * (Wavelength - 380) / (420 - 380);
        } else if ((Wavelength >= 420) && (Wavelength < 601)) {
            factor = 1.0;
        } else if ((Wavelength >= 601) && (Wavelength < 781)) {
            factor = 0.1 + 0.5 * (780 - Wavelength) / (780 - 600);
        } else {
            factor = 0.0;
        };

        // Don't want 0^x = 1 for x <> 0
        Color color = new Color((Red == 0.0 ? 0 : IntensityMax * Math.pow(Red * factor, Gamma) / 255),
                (Green == 0.0 ? 0 : IntensityMax * Math.pow(Green * factor, Gamma) / 255),
                (Blue == 0.0 ? 0 : IntensityMax * Math.pow(Blue * factor, Gamma) / 255),
                1.0);

        return color;
    }

}
