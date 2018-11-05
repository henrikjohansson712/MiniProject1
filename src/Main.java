import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        Player player = createPlayer(terminal);
        List<Attacker> attackers = new ArrayList<>();
        List<Bullet> bullets = new ArrayList<>();
        final int timeCounter1Threshold = 80;
        final int timeCounter2Threshold = 20;
        int attackerTimeCounter = 0;
        int bulletTimeCounter = 0;
        drawPlayer(terminal, player);


        while (isPlayerAlive(player, attackers)) {

            KeyStroke keyStroke;


            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();

                attackerTimeCounter++;

                if (attackerTimeCounter >= timeCounter1Threshold) {
                    attackerTimeCounter = 0;

                    addRandomAttackers(attackers, terminal);
                    moveAttackers(attackers);
                    stopAttackers(attackers, terminal);
                    drawAttackers(attackers, terminal);


                    terminal.flush();
                }

                bulletTimeCounter++;

                if (bulletTimeCounter >= timeCounter2Threshold) {
                    bulletTimeCounter = 0;

                    moveBullets(bullets);
                    stopBullets(bullets, terminal);
                    drawBullets(bullets, terminal);


                    terminal.flush();
                }
                destroyAttackers(attackers, bullets, terminal);
                terminal.flush();


            } while (keyStroke == null && isPlayerAlive(player, attackers));

            fireBullets(player, bullets, terminal, keyStroke);

            if (isPlayerAlive(player, attackers)) {
                movePlayer(player, keyStroke);
                drawPlayer(terminal, player);

            }

            terminal.flush();

        }

    }

    private static Player createPlayer(Terminal terminal) throws IOException {

        return new Player(terminal.getTerminalSize().getColumns() / 2, terminal.getTerminalSize().getRows() - 1, '\u04C1');

    }

    private static void drawPlayer(Terminal terminal, Player player) throws IOException {

        terminal.setCursorPosition(player.getPreviousX(), player.getPreviousY());
        terminal.putCharacter(' ');

        terminal.setCursorPosition(player.getX(), player.getY());
        terminal.putCharacter(player.getSymbol());

    }


    private static void drawAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        for (Attacker attacker : attackers) {
            terminal.setCursorPosition(attacker.getPreviousX(), attacker.getPreviousY());
            terminal.putCharacter(' ');

            terminal.setCursorPosition(attacker.getX(), attacker.getY());
            terminal.putCharacter(attacker.getSymbol());

        }

    }

    private static void addRandomAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {

        double probability = ThreadLocalRandom.current().nextDouble();

        if (probability <= 0.4) {
            attackers.add(new Attacker(ThreadLocalRandom.current().nextInt(terminal.getTerminalSize().getColumns()), 0, '\u2362'));

        }

    }

    private static void stopAttackers(List<Attacker> attackers, Terminal terminal) throws IOException {
        List<Attacker> attackersToStop = new ArrayList<>();
        for (Attacker attacker : attackers) {
            if (attacker.getY() >= terminal.getTerminalSize().getRows()) {
                attackersToStop.add(attacker);
            }
        }
        attackers.removeAll(attackersToStop);
    }

    private static void drawBullets(List<Bullet> bullets, Terminal terminal) throws IOException {

        for (Bullet bullet : bullets) {
            if (bullet != null) {
                terminal.setCursorPosition(bullet.getPreviousX(), bullet.getPreviousY());
                terminal.putCharacter(' ');
                terminal.setCursorPosition(bullet.getX(), bullet.getY());
                terminal.putCharacter(bullet.getSymbol());
            }
        }
    }

    private static void fireBullets(Player player, List<Bullet> bullets, Terminal terminal, KeyStroke keyStroke) throws IOException, InterruptedException {
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

    private static boolean isPlayerAlive(Player player, List<Attacker> attackers) {
        for (Attacker attacker : attackers) {
            if (attacker.getX() == player.getX() && attacker.getY() == player.getY()) {
                return false;
            }
        }
        return true;
    }

    private static void destroyAttackers(List<Attacker> attackers, List<Bullet> bullets, Terminal terminal) throws IOException {
//        List<Attacker> attackersToRemove = new ArrayList<>();
//        List<Bullet> bulletsToRemove = new ArrayList<>();

        Set<Attacker> attackersToRemove = new HashSet<>();
        Set<Bullet> bulletsToRemove = new HashSet<>();
        for (Attacker attacker : attackers) {
            for (Bullet bullet : bullets) {
                if (attacker.getX() == bullet.getX() &&
                        ( attacker.getY() == bullet.getY() ||
                          attacker.getY()-1 == bullet.getY()
                        )
                ) {
                    attackersToRemove.add(attacker);
                    bulletsToRemove.add(bullet);
//                    terminal.setCursorPosition(attacker.getX(), attacker.getY());
//                    terminal.putCharacter(' ');
//                    terminal.setCursorPosition(bullet.getX(), bullet.getY());
//                    terminal.putCharacter(' ');

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
}
// addBullets


//    attacker: \u2362 or \u2182
//    player: \u04C1
//    up arrow: \

//    left arrow: \u219E
//    right arrow: \u21A0