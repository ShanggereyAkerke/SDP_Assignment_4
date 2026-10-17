package theater.subsystem;

/** Stores and changes the physical screen position in the simulation. */
public class Screen {
    private boolean lowered;

    public void lower() {
        lowered = true;
        System.out.println("Screen lowered");
    }

    public void raise() {
        lowered = false;
        System.out.println("Screen raised");
    }

    public boolean isLowered() {
        return lowered;
    }
}
