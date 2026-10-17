# Assignment 4 defense guide

## First explanation

“My project implements the Facade structural pattern using a home theater.
The client calls `watchMovie` and `endMovie`. The facade coordinates five
devices in the correct order. Each device stores its own state and checks
its own rules. For example, the projector needs a lowered screen and a playing
DVD. The facade hides the sequence and cleanup from the ordinary client.”

## Show these files in this order

1. `Client.java`: one facade object and two operation calls. No subsystem imports.
2. `HomeTheaterFacade.java`: five private device fields and the startup sequence.
3. `Projector.java`: `project` checks the screen position and DVD playback.
4. `Amplifier.java`: `selectInput` retains the DVD source; `playSound` checks it.
5. `HomeTheaterFacade.java`: the catch block and `resetTheater` cleanup loop.
6. `docs/uml.png`: explain the arrows and their directions.
7. Run `Client.main`, then `FacadeChecks.main`. The test runner reports 31 scenarios.

## Follow one movie through the code

`watchMovie("Inception")` first checks the title and whether a session is active.
Then it sets the lights to 15 percent and lowers the screen. It switches the
DVD player on and loads the title. It switches the projector on and chooses
widescreen. It switches the amplifier on, selects DVD input, and sets volume
to 30. After the DVD starts, the projector checks its video source and screen,
and the amplifier checks its audio source. Only then is `movieActive` set to true.

`endMovie()` switches the amplifier and projector off, stops and switches off the
DVD, raises the screen, and restores the lights. The cleanup loop tries every
action even if an earlier action throws an exception.

## Likely questions and short answers

**What is Facade?** A structural design pattern that provides a simpler interface
to a more complex subsystem.

**Why does this project need it?** A client would otherwise create five devices,
make 12 startup calls in a valid order, and handle cleanup itself.

**What is actually hidden?** Device creation, configuration values, startup order,
device-to-device wiring, and coordination failure cleanup.

**Why is this more than printing messages?** Devices store state and reject invalid
operations. The projector reads the screen and DVD state. The amplifier stores
the DVD input and checks whether it is playing. Printed messages show the state
changes; they do not replace them. The hardware itself is simulated.

**Does the facade implement DVD playback rules?** No. `DvdPlayer` checks its power
and loaded movie. The facade calls the device operations in order.

**Can I bypass the facade?** Yes. All subsystem classes and their operations are
public and never import the facade. The last behavioral test demonstrates direct
use. The ordinary `Client` still uses only the facade, as the assignment requires.

**Why does the test create devices directly?** Tests are advanced callers that
inspect device state and inject failures. They are separate from the ordinary
application client. The injection constructor has package access, not public access.

**What happens if startup fails?** The facade tries to reset every device, then
throws `TheaterException`. `getCause()` retains the original startup failure.
Cleanup failures are attached using `addSuppressed()`.

**What is a suppressed exception?** An additional failure attached to the main
exception so that cleanup problems do not hide the original problem.

**Does cleanup always succeed?** No. It attempts every action and reports failures.
A stuck device may remain on. The caller can fix that problem and retry `endMovie`.

**Why validate a title in two places?** The facade rejects bad client input before
changing any device. The public DVD class also protects itself when used directly.
These checks protect two separate entry points.

**Why use constants?** `MOVIE_BRIGHTNESS` and `MOVIE_VOLUME` explain what 15 and 30
mean and provide one place to change each default.

**How is this different from Adapter?** Adapter makes an incompatible interface
usable. This facade simplifies a workflow across existing compatible classes.

**How is this different from Mediator?** Devices do not communicate through the
facade. They can still interact directly. The facade gives the client a simpler
entry point; it does not become the communication center for all devices.

**Which UML arrows are used?** Dashed arrows show use dependencies. Solid arrows
show stored references. An association marked `1` means exactly one reference;
the amplifier's `0..1` DVD input may be absent before selection or after shutdown.

**Which Clean Code principles are shown?** Meaningful names, single responsibility,
encapsulation, focused methods, avoiding duplication, and explicit error handling.
Use the annotated examples in report Section 3 to explain each one.

**What are the limits?** One session, one thread, simulated equipment, fixed defaults,
and reset to a standard state. It does not remember previous room settings.

## Rehearse without reading

Explain the two client calls. Name all five devices. Explain why playback starts
before projection and audio. Explain one invalid operation and one failure test.
Then explain what remains accessible without the facade. If any code is unfamiliar,
study or simplify it before presenting it as your own individual work.
