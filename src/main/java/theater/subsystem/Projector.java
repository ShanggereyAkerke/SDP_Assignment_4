package theater.subsystem;

import java.util.Objects;

/** Projects a playing DVD onto a lowered screen. */
public class Projector {
    private boolean on;
    private boolean wideScreen;

    public void on() {
        on = true;
        System.out.println("Projector on");
    }

    public void wideScreenMode() {
        requirePower();
        wideScreen = true;
        System.out.println("Projector mode: widescreen");
    }

    public void project(DvdPlayer player, Screen screen) {
        requirePower();
        Objects.requireNonNull(player, "Video source must not be null.");
        Objects.requireNonNull(screen, "Screen must not be null.");
        if (!screen.isLowered()) {
            throw new IllegalStateException("Lower the screen before projection.");
        }
        if (!player.isPlaying()) {
            throw new IllegalStateException("Video source must be playing.");
        }
        System.out.println("Projector displaying DVD in "
                + (wideScreen ? "widescreen" : "standard") + " mode");
    }

    public void off() {
        on = false;
        wideScreen = false;
        System.out.println("Projector off");
    }

    public boolean isOn() {
        return on;
    }

    public boolean isWideScreen() {
        return wideScreen;
    }

    private void requirePower() {
        if (!on) {
            throw new IllegalStateException("Projector must be on.");
        }
    }
}
