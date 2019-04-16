import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class Main {
    public static void main(String[] args) {

        /* Multiplayer Game */
        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
        gameRunner.setLeagueLevel(4);

        gameRunner.addAgent(Agent1.class);
        gameRunner.addAgent(Agent1.class);

        // Another way to add a player
        // gameRunner.addAgent("python3 /home/user/player.py");
        
        gameRunner.start();
    }
}
