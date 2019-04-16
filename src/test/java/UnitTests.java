import com.codingame.game.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UnitTests {

    public static void main(String[] args) {
        try {
            Class c = UnitTests.class;
            Method[] m = c.getDeclaredMethods();
            int testCount = 0;
            int failCount = 0;
            for (int i = 0; i < m.length; i++){
                if(m[i].getName().contains("Test")){
                    UnitTests testClass = new UnitTests();
                    boolean result = false;
                    try{
                        result = (boolean)m[i].invoke(testClass);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.err.println((result ? "PASSED" : "FAILED") + " " + m[i].getName());

                    if(result) testCount++;
                    else failCount++;
                }
            }

            System.err.println("------------------------------------");
            System.err.println("Total tests: " + (testCount+failCount) + " Failed: " + failCount);

        } catch (Exception e) {
            System.err.println("TESTRUN failed: " + e.getMessage());
        }
    }

    private List<Player> players;
    private Game game;

    public UnitTests(){
        players = new ArrayList<>();
        players.add(new Player());
        players.add(new Player());
    }


    public boolean Test_CollidingPods(){
        setupGame(2, 0);

        setupCar(0,0, -593, -155, -8, 262);
        setupCar(0,1, -494, 968, -80, 37);
        setupCar(1,0, 593, -155, 8, 262);
        setupCar(1,1, 494, 968, 80, 37);
        for(int i = 0; i < 300; i++){
            for(Player p : players){
                for(Car c : p.Cars){
                    c.handleInput(0, 0, 200);
                }
            }

            if(!assertCars(players.get(0).Cars.get(0), players.get(1).Cars.get(0)) || !assertCars(players.get(0).Cars.get(1), players.get(1).Cars.get(1)))
            {
                System.err.println("Failed on I: " + i);
                return false;
            }
            game.gameLoop();
            if(!assertCars(players.get(0).Cars.get(0), players.get(1).Cars.get(0)) || !assertCars(players.get(0).Cars.get(1), players.get(1).Cars.get(1)))
            {
                System.err.println("Failed on I: " + i);
                return false;
            }
        }

        return true;
    }

    public boolean Test_CollidingPods_case2(){
        setupGame(2, 0);

        setupCar(0,0, "-436 -376 -45 -38");
        setupCar(0,1, "-431 489 -39 45");
        setupCar(1,0, "436 -376 45 -38");
        setupCar(1,1, "431 489 39 45");
        for(int i = 0; i < 300; i++){
            for(Player p : players){
                for(Car c : p.Cars){
                    c.handleInput(0, 0, 200);
                }
            }

            if(!assertCars(players.get(0).Cars.get(0), players.get(1).Cars.get(0)) || !assertCars(players.get(0).Cars.get(1), players.get(1).Cars.get(1)))
            {
                System.err.println("BEFORE ON I: " + i);
                return false;
            }
            game.gameLoop();
            if(!assertCars(players.get(0).Cars.get(0), players.get(1).Cars.get(0)) || !assertCars(players.get(0).Cars.get(1), players.get(1).Cars.get(1)))
            {
                System.err.println("ERROR ON I: " + i);
                return false;
            }
        }

        return true;
    }

    public boolean Test_CollidingPods_SwappingBallsOn4(){
        setupGame(1, 0);

        setupCar(0,0, "4000 0 0 0");
        setupCar(1,0, "1500 0 0 0");

        Car p1 = players.get(0).Cars.get(0);
        Car p2 = players.get(1).Cars.get(0);
        p1.ball = new Ball(0,0);
        Ball b = p1.ball;


        for(int i = 0; i < 4; i++){
            Player p = players.get(0);
            for(Car c : p.Cars){
                c.handleInput(0, 0, 200);
            }

            game.gameLoop();
        }

        return assertTrue(null== p1.ball, "ball1") && assertTrue(b == p2.ball, "ball2");
    }

    public boolean Test_CollidingPods_BounceSwaps(){
        setupGame(1, 0);

        setupCar(0,0, "4300 0 0 0");
        setupCar(1,0, "3300 0 0 0");

        Car p1 = players.get(0).Cars.get(0);
        Car p2 = players.get(1).Cars.get(0);
        p1.ball = new Ball(0,0);
        Ball b = p1.ball;

        for(Player p : players){
            for (Car c : p.Cars) {
                c.handleInput(9000, 0, 200);
            }
        }

        game.gameLoop();
        return assertTrue(null== p1.ball, "ball1") && assertTrue(b == p2.ball, "ball2");
    }


    public boolean Test_CollidingPods_NotSwappingBallsOn3(){
        setupGame(1, 0);

        setupCar(0,0, "3500 0 0 0");
        setupCar(1,0, "1500 0 0 0");

        Car p1 = players.get(0).Cars.get(0);
        Car p2 = players.get(1).Cars.get(0);
        p1.ball = new Ball(0,0);
        Ball b = p1.ball;


        for(int i = 0; i < 3; i++){
            Player p = players.get(0);
            for(Car c : p.Cars){
                c.handleInput(0, 0, 200);
            }

            game.gameLoop();
        }

        return assertTrue(b== p1.ball, "ball1") && assertTrue(null == p2.ball, "ball2");
    }

    public boolean Test_TurningCars_CantDecide(){
        setupGame(1, 0);

        setupCar(0,0, "3500 0 0 0");

        Car p1 = players.get(0).Cars.get(0);
        p1.handleInput((int)p1.x, (int)p1.y, 0);


        return assertDouble(0, p1.angle, "Angle");
    }

    public boolean Test_TurningCars_NoThrust(){
        setupGame(1, 0);

        setupCar(0,0, "3500 0 0 0");

        Car p1 = players.get(0).Cars.get(0);
        p1.handleInput(0, 0, 0);


        return assertDouble(p1.getAngle(Constants.CENTER), p1.angle, "Angle");
    }


    public boolean Test_rounding2(){
        setupGame(2, 0);
        Car c = game.entities.get(0);
        c.vx = -29.99999999999998;

        Car c2 = game.entities.get(2);
        c2.vx = 30.000000000000007;

        c.adjust();
        c2.adjust();

        return assertOppositeDouble(c.vx, c2.vx, "vx");
    }

    public boolean Test_rounding(){
        setupGame(2, 0);
        Car c = game.entities.get(0);
        c.vx = -30.000000000000007;

        Car c2 = game.entities.get(2);
        c2.vx = 29.99999999999998;

        c.adjust();
        c2.adjust();

        return assertOppositeDouble(c.vx, c2.vx, "vx");
    }

    private void setupGame(int cars, int balls){
        Constants.Random = new Random(42);
        game = new Game(cars, balls, players);

    }

    private void setupCar(int player, int car, int x, int y, int vx, int vy){
        Player p = players.get(player);
        Car c = p.Cars.get(car);
        c.x = x;
        c.y = y;
        c.vx = vx;
        c.vy = vy;
        c.angle = 0;
    }

    private void setupCar(int player, int car, String s){
        Player p = players.get(player);
        Car c = p.Cars.get(car);
        String[] splitted = s.split(" ");

        c.x = Integer.parseInt(splitted[0]);
        c.y = Integer.parseInt(splitted[1]);
        c.vx = Integer.parseInt(splitted[2]);
        c.vy = Integer.parseInt(splitted[3]);
        c.angle = 0;
    }

    private boolean assertCars(Car a, Car b){
        return assertOppositeDouble(a.x, b.x, "x") &
                assertOppositeDouble(a.vx, b.vx, "vx") &
                assertDouble(a.y, b.y, "y") &
                assertDouble(a.vy, b.vy, "vy");
    }

    private static < T > boolean assertValue(T expected, T actual, String varName){
        if((expected==null) != (actual==null))
            return throwError("Values("+varName+") not equal - \nexpected: "  +expected + "\nactual:   " + actual);
        if(!expected.equals(actual))
            return throwError("Values("+varName+") not equal - \nexpected: "  +expected + "\nactual:   " + actual);
        return true;
    }

    private static boolean assertTrue(boolean value, String varName)
    {
        if(!value)
            return throwError("Values("+varName+") not equal: \nexpected true!");
        return true;
    }

    private static boolean assertDouble(double expected, double actual, String varName)
    {
        if(Math.abs(expected-actual) > Constants.EPSILON)
            return throwError("Values("+varName+") not equal: \nexpected: "  +expected + "\nactual:   " + actual);
        return true;
    }

    private static boolean assertOppositeDouble(double expected, double actual, String varName)
    {
        if(Math.abs(expected+actual) > Constants.EPSILON)
            return throwError("Values("+varName+") not equal: \nexpected: "  +expected + "\nactual:   " + actual);
        return true;
    }

    private static boolean throwError(String message){
        System.err.println("----------");
        System.err.println(message);
        System.err.println("----------");
        return false;
    }

}
