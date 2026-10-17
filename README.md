# Assignment 4 — Facade Pattern

## Description

This project demonstrates the Facade design pattern using a home theater simulation in Java.

`HomeTheaterFacade` coordinates five devices through two methods:

- `watchMovie(String title)` — prepares the devices and starts playback.
- `endMovie()` — stops playback and resets the theater.

The client uses only the facade. Each device manages its own state and validates its operations. The subsystem classes can also be used directly without the facade.

## Subsystem Classes

- **DvdPlayer** — loads movies and manages playback.
- **Amplifier** — selects the audio source and controls volume.
- **Projector** — checks the DVD and screen before projection.
- **Screen** — lowers and raises the screen.
- **TheaterLights** — adjusts room brightness.
