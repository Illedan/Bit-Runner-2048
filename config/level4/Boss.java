import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Made by Illedan, pb4 and Agade
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int rad2 = Integer.parseInt(in.nextLine());
        int rad1 = Integer.parseInt(in.nextLine());
        double impulse = Double.parseDouble(in.nextLine());
        int carCount = in.nextInt(); // the number of pucks you control

        // game loop
        while (true) {
            int myscore = in.nextInt(); // your score
            int enemyscore = in.nextInt(); // the other player's score
            int currentWinner = in.nextInt(); // winner as score is now, in case of a tie. -1: you lose, 0: draw, 1: you win
            int entities = in.nextInt(); // number of entities this round
            for (int i = 0; i < entities; i++) {
                int id = in.nextInt(); // the ID of this unit
                int type = in.nextInt(); // type of entity. 0 is your puck, 1 is enemy puck, 2 is balls
                int x = in.nextInt(); // position x relative to center 0
                int y = in.nextInt(); // position y relative to center 0
                int vx = in.nextInt(); // horizontal speed. Positive is right
                int vy = in.nextInt(); // vertical speed. Positive is downwards
                int angle = in.nextInt(); // facing angle of this puck
                int ballId = in.nextInt(); // id of carried ball, -1 if none
            }
            for (int i = 0; i < puckCount; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                System.out.println("0 0 200"); // X Y THRUST MESSAGE
            }
        }
    }
}