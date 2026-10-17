package theater.facade;

import java.util.List;
import java.util.Objects;
import theater.subsystem.Amplifier;
import theater.subsystem.DvdPlayer;
import theater.subsystem.Projector;
import theater.subsystem.Screen;
import theater.subsystem.TheaterLights;

/** Coordinates one movie session in a single-threaded console application. */
public final class HomeTheaterFacade {
    private static final int MOVIE_BRIGHTNESS = 15;
    private static final int MOVIE_VOLUME = 30;

    private final DvdPlayer dvd;
    private final Amplifier amplifier;
    private final Projector projector;
    private final Screen screen;
    private final TheaterLights lights;
    private boolean movieActive;

    public HomeTheaterFacade() {
        this(new DvdPlayer(), new Amplifier(), new Projector(),
                new Screen(), new TheaterLights());
    }

    // Package access permits failure testing without expanding the public API.
    HomeTheaterFacade(DvdPlayer dvd, Amplifier amplifier, Projector projector,
                      Screen screen, TheaterLights lights) {
        this.dvd = Objects.requireNonNull(dvd);
        this.amplifier = Objects.requireNonNull(amplifier);
        this.projector = Objects.requireNonNull(projector);
        this.screen = Objects.requireNonNull(screen);
        this.lights = Objects.requireNonNull(lights);
    }

    public void watchMovie(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank.");
        }
        if (movieActive) {
            throw new IllegalStateException("End the current movie before starting another.");
        }

        try {
            lights.dim(MOVIE_BRIGHTNESS);
            screen.lower();
            dvd.on();
            dvd.load(title);
            projector.on();
            projector.wideScreenMode();
            amplifier.on();
            amplifier.selectInput(dvd);
            amplifier.setVolume(MOVIE_VOLUME);
            dvd.play();
            projector.project(dvd, screen);
            amplifier.playSound();
            movieActive = true;
            System.out.println("Movie session started");
        } catch (RuntimeException cause) {
            TheaterException failure = new TheaterException("Unable to start movie.", cause);
            TheaterException cleanupFailure = resetTheater();
            if (cleanupFailure != null) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public void endMovie() {
        TheaterException cleanupFailure = resetTheater();
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
        System.out.println("Theater ready");
    }

    private TheaterException resetTheater() {
        TheaterException failure = new TheaterException("Unable to fully reset theater.");
        // Every cleanup action must be attempted, even when an earlier one fails.
        for (Runnable action : List.<Runnable>of(amplifier::off, projector::off,
                dvd::stop, dvd::off, screen::raise, lights::on)) {
            try {
                action.run();
            } catch (RuntimeException cause) {
                failure.addSuppressed(cause);
            }
        }
        movieActive = false;
        return failure.getSuppressed().length == 0 ? null : failure;
    }
}
