# Assignment 4 — Home Theater Facade

Java 17 console simulation of the Facade pattern. One client calls
`watchMovie("Inception")` and `endMovie()` instead of coordinating five devices.
The devices keep state, validate operations, and interact with each other.
This project simulates equipment; it does not control physical hardware or play video.

## Run

Install JDK 17 or later. In IntelliJ IDEA, open this folder, select JDK 17,
mark `src/main/java` as Sources Root, and run `theater.client.Client.main()`.
Mark `src/test/java` as Test Sources Root to run `theater.facade.FacadeChecks.main()`.
The project needs no external Java libraries or build-tool downloads.

From a macOS/Linux terminal opened in this folder:

```sh
sh run.sh
sh test.sh
```

On Windows, IntelliJ can run both main methods without the shell scripts.

## Structure

```text
src/main/java/theater/client/Client.java       Ordinary client
src/main/java/theater/facade/                  Facade and coordination exception
src/main/java/theater/subsystem/               Five independent device classes
src/test/java/theater/facade/FacadeChecks.java Local behavioral tests
docs/                                        Report, UML, and defense guide
.github/workflows/java.yml                    Java 17 CI configuration
```

## Design

`HomeTheaterFacade` supplies a public no-argument constructor and two public
operations. Its package-private constructor lets tests supply failing devices
without exposing device objects in the public client API.

Startup dims lights, lowers the screen, loads the powered DVD player, configures
projection and audio, starts playback, then projects the video and plays audio.
`Amplifier` retains a `DvdPlayer` input; `Projector` checks the player and screen
passed to `project`. The subsystem never imports the facade or client.
Public subsystem classes remain available to advanced callers.

Invalid titles and duplicate starts are rejected before changing devices.
If startup fails, the facade attempts all six cleanup actions and throws
`TheaterException` with the original cause. Cleanup errors are suppressed
exceptions, so one failed shutdown does not prevent the remaining actions.
`endMovie()` is safe before startup and can be repeated. A failed cleanup is
reported; callers can retry `endMovie()` after addressing the device problem.
The reset policy is devices off, screen raised, and lights at 100 percent.
It does not restore a caller's earlier custom settings. The simulation is single-threaded.

## Verification

`test.sh` compiles with `--release 17 -Xlint:all -Werror` and checks 31 behavioral
scenarios, including failure after each of the 12 startup operations, cleanup
failures, playback prerequisites, input boundaries, and direct subsystem use.
These are local project checks, not instructor tests. The preparation machine
used JDK 25.0.2 with Java 17 compatibility targeting; the included GitHub workflow
selects JDK 17, but its remote result must be checked after publishing.

The complete demo output is in `docs/demo-output.txt`. The final two lines are:

```text
Lights brightness: 100%
Theater ready
```

## Submission

Read `docs/SUBMISSION_CHECKLIST.md`. Publish the existing Git history, add the
actual repository URL to the report, and upload the report and link to Moodle.
Review the code and rehearse `docs/DEFENSE_GUIDE.md` before the live defense.

## References

- Assignment 4 Facade Pattern instructions supplied with the task.
- [Facade pattern reference](https://refactoring.guru/design-patterns/facade).
- [Java 17 Throwable API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Throwable.html).
