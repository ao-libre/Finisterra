package ar.com.tamborindeguy.client.utils;

import com.badlogic.gdx.graphics.Color;

public class Colors {

    public static final Color MANA = new Color((float) 27 / 255, (float) 156 / 255, (float) 252 / 255, 1);
    public static final Color HEALTH = new Color((float) 253 / 255, (float) 114 / 255, (float) 114 / 255, 1);
    public static final Color EXP = new Color((float) 99 / 255, (float) 110 / 255, (float) 114 / 255, 1);

    public static final Color GM = rgb(46, 204, 113);
    public static final Color NEWBIE = rgb(155, 89, 182);
    public static final Color CITIZEN = rgb(52, 152, 219);
    public static final Color CRIMINAL = rgb(231, 76, 60);
    public static final Color COMBAT = rgb(255, 76, 60);
    public static final Color GREY = rgb(149, 165, 166);


    private static Color rgb(int r, int g, int b) {
        return new Color((float) r / 255, (float) g / 255, (float) b / 255, 1);
    }

}
