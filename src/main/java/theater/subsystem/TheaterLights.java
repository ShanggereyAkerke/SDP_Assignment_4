package theater.subsystem;

/** Controls room brightness as a percentage. */
public class TheaterLights {
    private int brightness = 100;

    public void dim(int level) {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100.");
        }
        brightness = level;
        System.out.println("Lights brightness: " + brightness + "%");
    }

    public void on() {
        dim(100);
    }

    public int getBrightness() {
        return brightness;
    }
}
