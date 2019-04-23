

import java.util.ArrayList;
import java.util.Scanner;

public class Agent1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int rad2 = Integer.parseInt(scanner.nextLine());
        int rad1 = Integer.parseInt(scanner.nextLine());
        int impulse = Integer.parseInt(scanner.nextLine());
        int cars = Integer.parseInt(scanner.nextLine());
        System.err.println("RAD0:" + rad2 + " RAD1:" + rad1 + " IMPULSE: " + impulse);
        while (true) {
            int myscore = Integer.parseInt(scanner.nextLine());
            int enemyScore = Integer.parseInt(scanner.nextLine());
            int currentWinner = Integer.parseInt(scanner.nextLine());
            int entities = Integer.parseInt(scanner.nextLine());
            System.err.println("SCORES: " + myscore + " - " + enemyScore);
            System.err.println("WINNER: " + currentWinner);

            ArrayList<Car> carPoses = new ArrayList<>();
            ArrayList<Car> balls = new ArrayList<>();
            // MY CARS
            for(int i = 0; i < entities; i++){
                String s = scanner.nextLine(); // //{car.id, type, (int)car.x, (int)car.y, (int)car.vx, (int)car.vy, angle, ballId};
                String[] line = s.split(" ");
                System.err.println("INN: " + s);
                int type = Integer.parseInt(line[1]);
                int x = Integer.parseInt(line[2]);
                int y = Integer.parseInt(line[3]);
                int ballId = Integer.parseInt(line[7]);
                if(type == 0)
                    carPoses.add(new Car(x, y, ballId));
                if(type == 2)
                    balls.add(new Car(x, y, ballId));
            }

            Car target = new Car(0,0,0);
            if(balls.size()>0) target = balls.get(0);
            for(int i = 0; i < cars; i++){
                Car car = carPoses.get(i);
                if(car.ballId != -1) target = new Car(0,0,0);
                int thrust = 200;
                String s = i == 0? "Illedan" : "pb4";
                if(target.distance(car) < 500) thrust = 10;
                System.out.println(target.X + " " + target.Y + " " + thrust + " " + s);
            }
        }
    }

    public static class Car{
        public int X, Y, ballId;
        public Car(int x, int y, int ballId){
            X = x;
            Y = y;
            this.ballId = ballId;
        }

        public double distance(Car c2){
            return Math.sqrt(Math.pow(c2.X-X, 2) + Math.pow(c2.Y-Y, 2));
        }
    }
}
