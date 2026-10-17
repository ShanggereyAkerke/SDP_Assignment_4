package theater.client;

import theater.facade.HomeTheaterFacade;

/** The ordinary client knows only the facade. */
public final class Client {
    private Client() {
    }

    public static void main(String[] args) {
        HomeTheaterFacade theater = new HomeTheaterFacade();
        theater.watchMovie("Inception");
        theater.endMovie();
    }
}
