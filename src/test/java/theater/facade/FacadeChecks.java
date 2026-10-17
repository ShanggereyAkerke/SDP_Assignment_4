package theater.facade;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import theater.subsystem.Amplifier;
import theater.subsystem.DvdPlayer;
import theater.subsystem.Projector;
import theater.subsystem.Screen;
import theater.subsystem.TheaterLights;

/** Local behavioral checks; direct device access belongs to the test fixture only. */
public final class FacadeChecks {
    private static int passed;

    private FacadeChecks() {
    }

    public static void main(String[] args) {
        check("Successful startup configures all devices", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            require(f.dvd.isPlaying() && f.dvd.isOn(), "DVD must be playing");
            require(f.amplifier.isOn() && f.amplifier.getVolume() == 30, "Audio setup");
            require(f.projector.isOn() && f.projector.isWideScreen(), "Projection setup");
            require(f.screen.isLowered() && f.lights.getBrightness() == 15, "Room setup");
        });
        check("Startup calls devices in dependency order", () -> {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream original = System.out;
            try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                System.setOut(capture);
                new HomeTheaterFacade().watchMovie("Inception");
            } finally {
                System.setOut(original);
            }
            String text = output.toString(StandardCharsets.UTF_8);
            int position = -1;
            for (String event : List.of("Lights brightness: 15%", "Screen lowered", "DVD player on",
                    "DVD loaded: Inception", "Projector on", "Projector mode: widescreen",
                    "Amplifier on", "Amplifier input: DVD", "Amplifier volume: 30",
                    "DVD playing: Inception", "Projector displaying DVD", "Amplifier playing DVD audio",
                    "Movie session started")) {
                int next = text.indexOf(event, position + 1);
                require(next > position, "Missing or out-of-order event: " + event);
                position = next;
            }
        });
        check("Shutdown restores every device", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            f.facade.endMovie();
            assertReset(f);
        });
        check("Shutdown before startup is safe", () -> {
            Fixture f = new Fixture();
            f.facade.endMovie();
            assertReset(f);
        });
        check("Repeated shutdown is safe", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            f.facade.endMovie();
            f.facade.endMovie();
            assertReset(f);
        });
        check("Two consecutive movie sessions work", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            f.facade.endMovie();
            f.facade.watchMovie("Interstellar");
            require(f.dvd.isPlaying(), "Second session did not start");
        });
        check("Duplicate startup preserves the current movie", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            expect(IllegalStateException.class, () -> f.facade.watchMovie("Interstellar"));
            require(f.dvd.isPlaying() && f.screen.isLowered(), "Existing session disturbed");
        });
        check("Null title is rejected before changing devices", () -> {
            Fixture f = new Fixture();
            expect(IllegalArgumentException.class, () -> f.facade.watchMovie(null));
            assertReset(f);
        });
        check("Blank title is rejected before changing devices", () -> {
            Fixture f = new Fixture();
            expect(IllegalArgumentException.class, () -> f.facade.watchMovie("  \t "));
            assertReset(f);
        });
        check("Invalid title does not stop an active session", () -> {
            Fixture f = new Fixture();
            f.facade.watchMovie("Inception");
            expect(IllegalArgumentException.class, () -> f.facade.watchMovie(" "));
            require(f.dvd.isPlaying(), "Invalid request stopped playback");
        });
        check("Title whitespace is normalized by the player", () -> {
            DvdPlayer dvd = new DvdPlayer();
            dvd.on();
            dvd.load("  Inception  ");
            dvd.play();
            require(dvd.isPlaying(), "Normalized movie did not play");
        });
        for (String step : List.of("lights", "screen", "dvdOn", "load", "projectorOn",
                "wideScreen", "amplifierOn", "input", "volume", "play", "project", "sound")) {
            check("Startup failure is rolled back at " + step, () -> {
                Fixture f = failingFixture(step);
                TheaterException failure = expect(TheaterException.class,
                        () -> f.facade.watchMovie("Inception"));
                require(failure.getCause() instanceof IllegalStateException, "Original cause lost");
                assertReset(f);
                f.facade.watchMovie("Retry");
                require(f.dvd.isPlaying(), "Retry after rollback failed");
            });
        }
        check("Cleanup continues after two device failures and can be retried", () -> {
            Amplifier amplifier = new Amplifier() {
                private boolean fail = true;
                @Override public void off() {
                    if (fail) { fail = false; throw new IllegalStateException("Amplifier stuck"); }
                    super.off();
                }
            };
            Projector projector = new Projector() {
                private boolean fail = true;
                @Override public void off() {
                    if (fail) { fail = false; throw new IllegalStateException("Projector stuck"); }
                    super.off();
                }
            };
            Fixture f = new Fixture(new DvdPlayer(), amplifier, projector, new Screen(), new TheaterLights());
            f.facade.watchMovie("Inception");
            TheaterException failure = expect(TheaterException.class, f.facade::endMovie);
            require(failure.getSuppressed().length == 2, "Must preserve both cleanup failures");
            require(f.amplifier.isOn() && f.projector.isOn(), "Failure must remain visible in device state");
            require(!f.dvd.isOn() && !f.screen.isLowered() && f.lights.getBrightness() == 100,
                    "Later cleanup actions did not run");
            f.facade.endMovie();
            assertReset(f);
        });
        check("Startup cause and cleanup failure are both preserved", () -> {
            DvdPlayer dvd = new DvdPlayer() {
                @Override public void load(String title) { throw new IllegalStateException("Disc unreadable"); }
            };
            Projector projector = new Projector() {
                @Override public void off() { throw new IllegalStateException("Shutdown failed"); }
            };
            Fixture f = new Fixture(dvd, new Amplifier(), projector, new Screen(), new TheaterLights());
            TheaterException failure = expect(TheaterException.class,
                    () -> f.facade.watchMovie("Inception"));
            require("Disc unreadable".equals(failure.getCause().getMessage()), "Wrong primary cause");
            require(failure.getSuppressed().length == 1, "Cleanup report missing");
            require(failure.getSuppressed()[0].getSuppressed().length == 1, "Device failure missing");
            require(!f.dvd.isOn() && f.lights.getBrightness() == 100, "Cleanup did not continue");
        });
        check("DVD enforces power and loaded-media prerequisites", () -> {
            DvdPlayer dvd = new DvdPlayer();
            expect(IllegalStateException.class, dvd::play);
            expect(IllegalStateException.class, () -> dvd.load("Film"));
            dvd.on();
            expect(IllegalStateException.class, dvd::play);
            expect(IllegalArgumentException.class, () -> dvd.load(" "));
            dvd.load("Film");
            dvd.play();
            expect(IllegalStateException.class, () -> dvd.load("Another"));
            dvd.off();
            dvd.on();
            expect(IllegalStateException.class, dvd::play);
        });
        check("Amplifier requires power and a playing input", () -> {
            Amplifier amp = new Amplifier();
            DvdPlayer dvd = new DvdPlayer();
            expect(IllegalStateException.class, amp::playSound);
            expect(IllegalStateException.class, () -> amp.setVolume(30));
            amp.on();
            expect(IllegalStateException.class, amp::playSound);
            expect(NullPointerException.class, () -> amp.selectInput(null));
            expect(IllegalStateException.class, () -> amp.selectInput(dvd));
            dvd.on();
            amp.selectInput(dvd);
            expect(IllegalStateException.class, amp::playSound);
            dvd.load("Film");
            dvd.play();
            amp.playSound();
            dvd.stop();
            expect(IllegalStateException.class, amp::playSound);
        });
        check("Volume rejects out-of-range values and accepts endpoints", () -> {
            Amplifier amp = new Amplifier();
            amp.on();
            amp.setVolume(0);
            amp.setVolume(100);
            expect(IllegalArgumentException.class, () -> amp.setVolume(-1));
            expect(IllegalArgumentException.class, () -> amp.setVolume(101));
            require(amp.getVolume() == 100, "Rejected volume changed state");
        });
        check("Projector checks screen position and playback state", () -> {
            Projector projector = new Projector();
            DvdPlayer dvd = new DvdPlayer();
            Screen screen = new Screen();
            expect(IllegalStateException.class, () -> projector.project(dvd, screen));
            expect(IllegalStateException.class, projector::wideScreenMode);
            projector.on();
            expect(IllegalStateException.class, () -> projector.project(dvd, screen));
            screen.lower();
            expect(IllegalStateException.class, () -> projector.project(dvd, screen));
            dvd.on();
            dvd.load("Film");
            dvd.play();
            projector.project(dvd, screen);
            screen.raise();
            expect(IllegalStateException.class, () -> projector.project(dvd, screen));
        });
        check("Lights reject out-of-range brightness and accept endpoints", () -> {
            TheaterLights lights = new TheaterLights();
            lights.dim(0);
            lights.dim(100);
            expect(IllegalArgumentException.class, () -> lights.dim(-1));
            expect(IllegalArgumentException.class, () -> lights.dim(101));
            require(lights.getBrightness() == 100, "Rejected brightness changed state");
        });
        check("Subsystem is usable directly without the facade", () -> {
            DvdPlayer dvd = new DvdPlayer();
            Screen screen = new Screen();
            Projector projector = new Projector();
            Amplifier amplifier = new Amplifier();
            screen.lower();
            dvd.on();
            dvd.load("Film");
            dvd.play();
            projector.on();
            projector.project(dvd, screen);
            amplifier.on();
            amplifier.selectInput(dvd);
            amplifier.setVolume(20);
            amplifier.playSound();
            require(dvd.isPlaying(), "Direct subsystem use failed");
        });
        System.out.println("All " + passed + " local behavioral scenarios passed.");
    }

    private static Fixture failingFixture(String step) {
        // Fail once after changing state, so rollback must undo partial startup.
        class Trigger {
            private boolean pending = true;
            void at(String current) {
                if (pending && step.equals(current)) {
                    pending = false;
                    throw new IllegalStateException("Injected failure: " + step);
                }
            }
        }
        Trigger trigger = new Trigger();
        DvdPlayer dvd = new DvdPlayer() {
            @Override public void on() { super.on(); trigger.at("dvdOn"); }
            @Override public void load(String title) { super.load(title); trigger.at("load"); }
            @Override public void play() { super.play(); trigger.at("play"); }
        };
        Amplifier amplifier = new Amplifier() {
            @Override public void on() { super.on(); trigger.at("amplifierOn"); }
            @Override public void selectInput(DvdPlayer source) { super.selectInput(source); trigger.at("input"); }
            @Override public void setVolume(int level) { super.setVolume(level); trigger.at("volume"); }
            @Override public void playSound() { super.playSound(); trigger.at("sound"); }
        };
        Projector projector = new Projector() {
            @Override public void on() { super.on(); trigger.at("projectorOn"); }
            @Override public void wideScreenMode() { super.wideScreenMode(); trigger.at("wideScreen"); }
            @Override public void project(DvdPlayer source, Screen target) {
                super.project(source, target); trigger.at("project");
            }
        };
        Screen screen = new Screen() {
            @Override public void lower() { super.lower(); trigger.at("screen"); }
        };
        TheaterLights lights = new TheaterLights() {
            @Override public void dim(int level) { super.dim(level); trigger.at("lights"); }
        };
        return new Fixture(dvd, amplifier, projector, screen, lights);
    }

    private static void assertReset(Fixture f) {
        require(!f.dvd.isOn() && !f.dvd.isPlaying(), "DVD not reset");
        require(!f.amplifier.isOn() && f.amplifier.getVolume() == 0, "Amplifier not reset");
        require(!f.projector.isOn() && !f.projector.isWideScreen(), "Projector not reset");
        require(!f.screen.isLowered(), "Screen not raised");
        require(f.lights.getBrightness() == 100, "Lights not restored");
    }

    private static void check(String name, Runnable scenario) {
        PrintStream original = System.out;
        try (PrintStream muted = new PrintStream(new ByteArrayOutputStream())) {
            System.setOut(muted);
            scenario.run();
        } catch (RuntimeException | AssertionError failure) {
            original.println("FAIL: " + name);
            throw failure;
        } finally {
            System.setOut(original);
        }
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static <T extends RuntimeException> T expect(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException failure) {
            if (type.isInstance(failure)) {
                return type.cast(failure);
            }
            throw new AssertionError("Expected " + type.getSimpleName() + " but got " + failure, failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static final class Fixture {
        final DvdPlayer dvd;
        final Amplifier amplifier;
        final Projector projector;
        final Screen screen;
        final TheaterLights lights;
        final HomeTheaterFacade facade;

        Fixture() {
            this(new DvdPlayer(), new Amplifier(), new Projector(), new Screen(), new TheaterLights());
        }

        Fixture(DvdPlayer dvd, Amplifier amplifier, Projector projector,
                Screen screen, TheaterLights lights) {
            this.dvd = dvd;
            this.amplifier = amplifier;
            this.projector = projector;
            this.screen = screen;
            this.lights = lights;
            facade = new HomeTheaterFacade(dvd, amplifier, projector, screen, lights);
        }
    }
}
