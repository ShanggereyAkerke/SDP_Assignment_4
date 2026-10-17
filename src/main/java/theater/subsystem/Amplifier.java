package theater.subsystem;

import java.util.Objects;

/** Selects a DVD audio source and controls its output volume. */
public class Amplifier {
    private boolean on;
    private int volume;
    private DvdPlayer input;

    public void on() {
        on = true;
        System.out.println("Amplifier on");
    }

    public void selectInput(DvdPlayer player) {
        requirePower();
        Objects.requireNonNull(player, "Audio source must not be null.");
        if (!player.isOn()) {
            throw new IllegalStateException("Audio source must be on.");
        }
        input = player;
        System.out.println("Amplifier input: DVD");
    }

    public void setVolume(int level) {
        requirePower();
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100.");
        }
        volume = level;
        System.out.println("Amplifier volume: " + volume);
    }

    public void playSound() {
        requirePower();
        if (input == null || !input.isPlaying()) {
            throw new IllegalStateException("Audio source must be playing.");
        }
        System.out.println("Amplifier playing DVD audio at volume " + volume);
    }

    public void off() {
        on = false;
        volume = 0;
        input = null;
        System.out.println("Amplifier off");
    }

    public boolean isOn() {
        return on;
    }

    public int getVolume() {
        return volume;
    }

    private void requirePower() {
        if (!on) {
            throw new IllegalStateException("Amplifier must be on.");
        }
    }
}
