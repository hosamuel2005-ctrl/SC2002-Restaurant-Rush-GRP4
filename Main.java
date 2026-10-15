public class Main {
    public static void main(String[] args) {
        GameUI ui = new GameUI();
        GameController game = new GameController(ui);

        game.startGame();
    }
}