import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static long scoreCounter = 0;

    public static void main(String[] args) {

        try {

            startGame();

        } catch (IOException | InterruptedException e) {

            e.printStackTrace();
            System.exit(1);
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

        Player player = createPlayer(terminal);

        List<Attacker> attackers = new ArrayList<>();
        List<Bullet> bullets = new ArrayList<>();

        final int attackerTimeCounterThreshold = 80;
        final int bulletTimeCounterThreshold = 20;
        int attackerTimeCounter = 0;
        int bulletTimeCounter = 0;

        terminal.setCursorPosition((terminal.getTerminalSize().getColumns() / 2) - 18, (terminal.getTerminalSize().getRows() / 2) - 2);
        terminal.setForegroundColor(TextColor.ANSI.CYAN);
        terminal.enableSGR(SGR.BOLD);
        String fatalError = "FatalError Games Proudly Presents...";

        for (char c : fatalError.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.setCursorPosition((terminal.getTerminalSize().getColumns() / 2) - 7, (terminal.getTerminalSize().getRows() / 2) - 1);
        String gameTitle = "Spejs Intruders";

        for (char c : gameTitle.toCharArray()) {
            terminal.putCharacter(c);
        }
        terminal.resetColorAndSGR();
        Thread.sleep(5000);
        terminal.clearScreen();

        drawPlayer(terminal, player);

        while (isPlayerAlive(player, attackers, terminal)) {

            KeyStroke keyStroke;

            do {

                Thread.sleep(5);
                keyStroke = terminal.pollInput();

                attackerTimeCounter++;

                if (attackerTimeCounter >= attackerTimeCounterThreshold) {
                    attackerTimeCounter = 0;

                    addRandomAttackers(attackers, terminal);
                    moveAttackers(attackers, terminal);
                    stopAttackers(attackers, terminal);
                    drawAttackers(attackers, terminal);
                    drawScore(terminal);

                    terminal.flush();
                }

                bulletTimeCounter++;

                if (bulletTimeCounter >= bulletTimeCounterThreshold) {
                    bulletTimeCounter = 0;

                    moveBullets(bullets);
                    stopBullets(bullets, terminal);
                    drawBullets(bullets, terminal);

                    terminal.flush();
                }

                destroyAttackers(attackers, bullets, terminal);
                terminal.flush();

            } while (keyStroke == null && isPlayerAlive(player, attackers, terminal));

            fireBullets(player, bullets, keyStroke);

            if (isPlayerAlive(player, attackers, terminal)) {
                movePlayer(player, keyStroke);
                drawPlayer(terminal, player);
            }

            terminal.flush();
        }

        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setForegroundColor(TextColor.ANSI.RED);
        terminal.enableSGR(SGR.BOLD);
        terminal.enableSGR(SGR.BLINK);
        String gameOver = "Game Over!";

        for (char c : gameOver.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.resetColorAndSGR();
        terminal.flush();

    }

    private static Player createPlayer(Terminal terminal) throws IOException {

        return new Player(terminal.getTerminalSize().getColumns() / 2, terminal.getTerminalSize().getRows() - 1, '\u04C1');
    }

    private static void drawPlayer(Terminal terminal, Player player) throws IOException {

        terminal.setCursorPosition(player.getPreviousX(), player.getPreviousY());
        terminal.putCharacter(' ');

        terminal.setCursorPosition(player.getX(), player.getY());
        terminal.setForegroundColor(TextColor.ANSI.MAGENTA);
        terminal.putCharacter(player.getSymbol());
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

    private static boolean isPlayerAlive(Player player, List<Attacker> attackers, Terminal terminal) throws IOException {

        for (Attacker attacker : attackers) {

            if ((attacker.getX() == player.getX() && attacker.getY() == player.getY()) || scoreCounter <= -100) {
                terminal.setCursorPosition((terminal.getTerminalSize().getColumns() / 2) - 5, (terminal.getTerminalSize().getRows() / 2) - 2);
                return false;
            }
        }
        return true;
    }

    private static void fireBullets(Player player, List<Bullet> bullets, KeyStroke keyStroke) {

        if (keyStroke == null || keyStroke.getCharacter() == null) {
            return;
        } else {
            switch (keyStroke.getCharacter()) {
                case 'q':
                    bullets.add(new Bullet(player.getX() - 1, player.getY(), '\u219E'));
                    break;
                case 'w':
                    bullets.add(new Bullet(player.getX(), player.getY() - 1, '\u219F'));
                    break;
                case 'e':
                    bullets.add(new Bullet(player.getX() + 1, player.getY(), '\u21A0'));
                    break;
                default:
                    break;
            }
        }
    }

    private static void drawBullets(List<Bullet> bullets, Terminal terminal) throws IOException {

        for (Bullet bullet : bullets) {

            if (bullet != null) {
                terminal.setCursorPosition(bullet.getPreviousX(), bullet.getPreviousY());
                terminal.putCharacter(' ');
                terminal.setCursorPosition(bullet.getX(), bullet.getY());
                terminal.setForegroundColor(TextColor.ANSI.YELLOW);
                terminal.putCharacter(bullet.getSymbol());
            }
        }
    }

    private static void moveBullets(List<Bullet> bullets) {

        for (Bullet bullet : bullets) {

            if (bullet.getSymbol() == '\u219F') {
                bullet.moveUp();
            } else if (bullet.getSymbol() == '\u219E') {
                bullet.moveLeft();
            } else if (bullet.getSymbol() == '\u21A0') {
                bullet.moveRight();
            }
        }
    }

    private static void stopBullets(List<Bullet> bullets, Terminal terminal) throws IOException {

        List<Bullet> bulletsToStop = new ArrayList<>();

        for (Bullet bullet : bullets) {

            if (bullet.getY() < 0
                    || bullet.getX() < 0
                    || bullet.getX() >= terminal.getTerminalSize().getColumns()) {
                bulletsToStop.add(bullet);
                terminal.setCursorPosition(bullet.getX(), bullet.getY());
                terminal.putCharacter(' ');
            }
        }
        bullets.removeAll(bulletsToStop);
    }

    private static void addRandomAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        double probability = ThreadLocalRandom.current().nextDouble();

        if (probability <= 0.4) {
            attackers.add(new Attacker(ThreadLocalRandom.current().nextInt(terminal.getTerminalSize().getColumns()), 0, '\u2362'));
        }
    }

    private static void drawAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        for (Attacker attacker : attackers) {
            terminal.setCursorPosition(attacker.getPreviousX(), attacker.getPreviousY());
            terminal.putCharacter(' ');

            terminal.setCursorPosition(attacker.getX(), attacker.getY());
            terminal.setForegroundColor(TextColor.ANSI.CYAN);
            terminal.putCharacter(attacker.getSymbol());
        }
    }

    private static void moveAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        for (Attacker attacker : attackers) {

            if (attacker.getY() < terminal.getTerminalSize().getRows() - 1) {
                attacker.attack();
            }
        }
    }

    private static void stopAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        List<Attacker> attackersToStop = new ArrayList<>();

        for (Attacker attacker : attackers) {

            if (attacker.getY() == terminal.getTerminalSize().getRows() - 1) {
                attackersToStop.add(attacker);
                terminal.setCursorPosition(attacker.getX(), attacker.getY());
                terminal.putCharacter(' ');
            }
        }

        for (Attacker attacker : attackersToStop) {
            attacker.countdownToRemove();

            if (attacker.countdownToRemove() <= 0) {
                scoreCounter -= 10;
                attackers.remove(attacker);
                terminal.setCursorPosition(attacker.getX(), attacker.getY());
                terminal.putCharacter(' ');
            }
        }
    }

    private static void destroyAttackers(List<Attacker> attackers, List<Bullet> bullets, Terminal terminal) throws IOException {

        Set<Attacker> attackersToRemove = new HashSet<>();
        Set<Bullet> bulletsToRemove = new HashSet<>();

        for (Attacker attacker : attackers) {

            for (Bullet bullet : bullets) {

                if (attacker.getX() == bullet.getX()
                        && (attacker.getY() == bullet.getY() || attacker.getY() - 1 == bullet.getY())) {
                    scoreCounter += 10;
                    attackersToRemove.add(attacker);
                    bulletsToRemove.add(bullet);
                }
            }
        }

        for (Bullet bullet : bulletsToRemove) {
            terminal.setCursorPosition(bullet.getX(), bullet.getY());
            terminal.putCharacter(' ');
        }

        for (Attacker attacker : attackersToRemove) {
            terminal.setCursorPosition(attacker.getX(), attacker.getY());
            terminal.putCharacter(' ');
        }

        attackers.removeAll(attackersToRemove);
        bullets.removeAll(bulletsToRemove);
    }

    private static void drawScore(Terminal terminal) throws IOException {

        terminal.setCursorPosition(0, 0);
        terminal.enableSGR(SGR.BOLD);
        terminal.setForegroundColor(TextColor.ANSI.WHITE);

        String score = "Score: ";
        String blanks = "    ";
        String scorePoints = Long.toString(scoreCounter);

        for (char c : score.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.setCursorPosition(score.length(), 0);

        for (char c : blanks.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.setCursorPosition(score.length(), 0);

        for (char c : scorePoints.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.resetColorAndSGR();
    }
}
