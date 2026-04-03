package org.juanse.ui.sound;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Maneja todos los sonidos del juego.
 * Principio S (SRP): solo se ocupa de reproducir sonidos.
 */
public class SoundManager {

    public static void playSound(String soundFile) {
        try {
            File file = new File("src/main/resources/sounds/" + soundFile);
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + soundFile);
        }
    }
}
