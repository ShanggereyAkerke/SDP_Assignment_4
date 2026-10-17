package theater.subsystem;

/** A stateful DVD player that can also be used without a facade. */
public class DvdPlayer {
    private boolean on;
    private boolean playing;
    private String movie;

    public void on() {
        on = true;
        System.out.println("DVD player on");
    }

    public void load(String title) {
        requirePower();
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank.");
        }
        if (playing) {
            throw new IllegalStateException("Stop playback before loading a movie.");
        }
        movie = title.strip();
        System.out.println("DVD loaded: " + movie);
    }

    public void play() {
        requirePower();
        if (movie == null) {
            throw new IllegalStateException("Load a movie before playback.");
        }
        playing = true;
        System.out.println("DVD playing: " + movie);
    }

    public void stop() {
        playing = false;
        System.out.println("DVD playback stopped");
    }

    public void off() {
        playing = false;
        movie = null;
        on = false;
        System.out.println("DVD player off");
    }

    public boolean isOn() {
        return on;
    }

    public boolean isPlaying() {
        return playing;
    }

    private void requirePower() {
        if (!on) {
            throw new IllegalStateException("DVD player must be on.");
        }
    }
}
