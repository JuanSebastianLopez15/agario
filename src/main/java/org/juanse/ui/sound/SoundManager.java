package org.juanse.ui.sound;

import javax.sound.sampled.*;
import java.net.URL;

/**
 * Maneja todos los sonidos del juego.
 * Principio S (SRP): solo se ocupa de reproducir sonidos.
 */
public class SoundManager {

    private static Clip backgroundClip;

    /**
     * Reproduce música de fondo en loop con volumen bajo.
     *
     * @param soundFile nombre del archivo de sonido
     */
    public static void playBackground(String soundFile) {
        stopBackground();
        try {
            URL url = SoundManager.class.getClassLoader().getResource("sounds/" + soundFile);
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audio);

            FloatControl volume = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-10.0f);

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir fondo: " + soundFile);
        }
    }

    /**
     * Detiene la música de fondo.
     */
    public static void stopBackground() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    /**
     * Reproduce un efecto de sonido una sola vez.
     *
     * @param soundFile nombre del archivo de sonido
     */
    public static void playEffect(String soundFile) {
        try {
            URL url = SoundManager.class.getClassLoader().getResource("sounds/" + soundFile);
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir efecto: " + soundFile);
        }
    }
}
