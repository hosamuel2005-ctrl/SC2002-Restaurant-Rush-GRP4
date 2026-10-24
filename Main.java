// Main class to run the Restaurant Rush game
public class Main {
    public static void main(String[] args) {
        GameUI ui = new GameUI();
        GameController game = new GameController(ui);

        try {
            game.startGame();
        } catch (java.util.NoSuchElementException e) {
            ui.showMessage("\nInput closed. Exiting the game.");
        }
    }
}