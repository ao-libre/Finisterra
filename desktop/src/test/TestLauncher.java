package test;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.esotericsoftware.minlog.Log;
import game.Config;
import game.Config.Init;
import game.Config.Init.Video;
import game.utils.Resources;
import shared.util.LogSystem;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Launcher genérico para correr pruebas
 * En Intellij IDEA, hacer click derecho en la clase y tocar Run 'TestLauncher.main()'
 * @todo Revisar
 */
public class TestLauncher {

    public static void main(String[] arg) {
        System.setProperty("org.lwjgl.opengl.Display.enableOSXFullscreenModeAPI", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Finisterra");

        Log.setLogger(new LogSystem());

        // Load desktop config.json or create default.
        Config config;
        if (Config.fileExists(Resources.CLIENT_CONFIG)) {
            config = Config.fileLoad(Resources.CLIENT_CONFIG);
        }
        else {
            Log.info("DesktopLauncher", "Config file " + Resources.CLIENT_CONFIG + " not found, creating default.");
            config = Config.getDefault();
            config.fileSave(Resources.CLIENT_CONFIG);
        }

        Init initConfig = config.initConfig;
        Video video = initConfig.video;

        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();

        // Build LWJGL configuration
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Finisterra - Argentum Online Java");
        cfg.setWindowedMode(displayMode.width, displayMode.height);
        //cfg.setFullscreenMode(displayMode);
        cfg.useVsync(video.getVsync());
        cfg.setIdleFPS(72);
        cfg.setResizable(initConfig.isResizeable());
        cfg.disableAudio(initConfig.isDisableAudio());
        cfg.setMaximized(initConfig.isStartMaximized());
        cfg.setWindowSizeLimits(854, 480, -1, -1);

        if (video.getHiDPIMode().equalsIgnoreCase("Pixels")) {
            cfg.setHdpiMode(HdpiMode.Pixels);
        } else {
            cfg.setHdpiMode(HdpiMode.Logical);
        }

        // Set the icon that will be used in the title bar.
        cfg.setWindowIcon(Resources.CLIENT_ICON);

        // Launch application.
        try {
            new Lwjgl3Application(new UITest(), cfg);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
            try {
                PrintWriter pw = new PrintWriter(new File("error.txt"));
                e.printStackTrace(pw);
                pw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
