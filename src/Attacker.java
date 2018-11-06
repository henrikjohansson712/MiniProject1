public class Attacker {

    private int x;
    private int y;
    private char symbol;
    private int previousX;
    private int previousY;
    private int startTime = 15;



    public Attacker(int x, int y, char symbol) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.previousX = x;
        this.previousY = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getSymbol() {
        return symbol;
    }
    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public void attack() {
        previousX = x;
        previousY = y;
        y++;
    }

    public int countdownToRemove (){
        return startTime --;


    }
}
