import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {

        try {
            startGame();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.out.println("Game Over!");
        }

    }

    private static void startGame() throws IOException, InterruptedException {

        Terminal terminal = createTerminal();

        gameLoop(terminal);

    }

    private static Terminal createTerminal() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);
        return terminal;
    }

    private static void gameLoop(Terminal terminal) throws IOException, InterruptedException {

        Player player = createPlayer();
        List<Attacker> attackers = new ArrayList<>();
        final int timeCounterThreshold = 80;
        int timeCounter = 0;


        while (true) {

            KeyStroke keyStroke;

            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();

                timeCounter++;

                if (timeCounter >= timeCounterThreshold) {

                    timeCounter = 0;

                    addRandomAttackers(attackers);
                    moveAttackers(attackers);
                    drawAttackers(attackers, terminal);
                    drawPlayer(terminal, player);

                    terminal.flush();
                }

            } while (keyStroke == null);

            movePlayer(player, keyStroke);
            drawPlayer(terminal, player);

            terminal.flush();

        }

    }

    private static Player createPlayer() {

        return new Player(40, 40, '\u04C1');

    }

//    attacker: \u2362 or \u2182
//    player: \u04C1
//    up arrow: \u219F
//    left arrow: \u219E
//    right arrow: \u21A0

    private static void drawPlayer(Terminal terminal, Player player) throws IOException {

        terminal.setCursorPosition(player.getPreviousX(), player.getPreviousY());
        terminal.putCharacter(' ');

        terminal.setCursorPosition(player.getX(), player.getY());
        terminal.putCharacter(player.getSymbol());

    }

    private static void drawAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        terminal.clearScreen();

        for (Attacker attacker : attackers) {
            terminal.setCursorPosition(attacker.getX(), attacker.getY());
            terminal.putCharacter(attacker.getSymbol());
        }

    }

    private static void addRandomAttackers(List<Attacker> attackers) {

        double probability = ThreadLocalRandom.current().nextDouble();

        if (probability <= 0.4) {
            attackers.add(new Attacker(ThreadLocalRandom.current().nextInt(180), 0, '\u2362'));
        }

    }

    private static void movePlayer(Player player, KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case ArrowLeft:
                player.moveLeft();
                break;
            case ArrowRight:
                player.moveRight();
                break;
        }
    }

    private static void moveAttackers(List<Attacker> attackers) {
        for (Attacker attacker : attackers) {
            attacker.attack();
        }
    }

//    private static KeyStroke getUserKeyStroke(Terminal terminal) throws InterruptedException, IOException {
//
//        KeyStroke keyStroke;
//        do {
//            Thread.sleep(5);
//            keyStroke = terminal.pollInput();
//        } while (keyStroke == null);
//        return keyStroke;
//
//    }

}
