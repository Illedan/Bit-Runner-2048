package com.codingame.game;
import java.lang.ref.WeakReference;
import java.util.*;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.core.Tooltip;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;
import debugModule.DebugModule;
import messageModule.MessageModule;
import tooltipModule.TooltipModule;

public class Referee extends AbstractReferee {
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private TooltipModule tooltipModule;
    @Inject private EndScreenModule endScreenModule;
    @Inject private MessageModule messageModule;
    @Inject private DebugModule debugModule;
    private ViewController viewController;
    private Game game;
    private boolean doDebug;

    @Override
    public void init() {
        Properties params = gameManager.getGameParameters();
        Constants.Random = new Random(getSeed(params));
        Constants.setupValues();

        gameManager.setMaxTurns(Constants.ShowWinners?50: 250);
        gameManager.setFrameDuration(228);
        gameManager.setTurnMaxTime(50);

        game = new Game(Constants.NUM_CARS, Constants.NUM_BALLS, gameManager.getPlayers());

        viewController = new ViewController(graphicEntityModule, tooltipModule, game, messageModule, debugModule);
        viewController.Init();
    }

    private Long getSeed(Properties params) {
        try {
            return Long.parseLong(params.getProperty("seed", "0"));
        } catch (NumberFormatException nfe) {
            return 0L;
        }
    }

    @Override
    public void onEnd() {

        //Solving draws.
        if(game.players.get(0).getScore() == game.players.get(1).getScore() && game.players.get(0).getScore() > 0){
            int currentWinner = getCurrentWinner(game.players.get(0), game.players.get(1));

            if(currentWinner == 0){
                gameManager.addToGameSummary("Equal time scoring, game is draw!");
            }

            else if(currentWinner == 1){
                game.players.get(0).setScore(game.players.get(0).getScore()+1);
                gameManager.addToGameSummary("Player 1 scored its last goal earlier and won!");
                gameManager.addTooltip(new Tooltip(game.players.get(0).getIndex(), "Player 1 scored its last goal earlier and won!"));
            }

            else{
                game.players.get(1).setScore(game.players.get(1).getScore()+1);
                gameManager.addToGameSummary("Player 2 scored its last goal earlier and won!");
                gameManager.addTooltip(new Tooltip(game.players.get(1).getIndex(), "Player 2 scored its last goal earlier and won!"));
            }
        }

        endScreenModule.setTitleRankingsSprite("splashlogo.png");
        endScreenModule.setScores(gameManager.getPlayers().stream().mapToInt(p -> p.getScore()).toArray());
    }

    @Override
    public void gameTurn(int turn) {
        for (Player player : gameManager.getActivePlayers()) {
            try{
                sendInputs(player, turn);
                player.execute();
            }
            catch (Exception e){
                player.deactivate(String.format("Invalid input"));
                player.setScore(-1);
                gameManager.addToGameSummary(e.getMessage());
            }
        }

        for (Player player : gameManager.getActivePlayers()) {
            List<String> outputs = new ArrayList<>();
            try {
                outputs = player.getOutputs();
                for(int i = 0; i < player.Cars.size(); i++){
                    handleInput(player.Cars.get(i), outputs.get(i));
                }
            } catch (TimeoutException e) {
                gameManager.addToGameSummary(String.format("$%d timeout!", player.getIndex()));
                    player.deactivate(String.format("$%d timeout!", player.getIndex()));
                    player.setScore(-1);
            }
            catch (Exception e){
                player.deactivate(String.format("Invalid input"));
                player.setScore(-1);
                StringBuilder sb = new StringBuilder();
                for (String s : outputs)
                {
                    sb.append(s);
                    sb.append(" ");
                }
                gameManager.addToGameSummary("Invalid input. Was:" + sb.toString());
                gameManager.addToGameSummary(e.getMessage());
            }
        }

        for(Integer playerId : game.scorings){
            gameManager.addTooltip(new Tooltip(playerId, String.format("$%d scored!", playerId)));
        }

        if(gameManager.getActivePlayers().size() < 2 || game.isGameOver()){
            viewController.update();
            gameManager.endGame();
            return;
        }

        game.gameLoop();
        viewController.update();

        //Debug info.
        if(doDebug){
            String collisions = "Collisions: ";
            for(Collision col : game.allOccuringCollisions){
                int bId = col.b==null? -1 : col.b.id;
                collisions += col.a.id + " " + bId + " " + col.time+";";
            }
            gameManager.addToGameSummary(collisions);
        }
    }

    private void sendInputs(Player player, int turn){
        if(turn==0){

            // Params in case a balance is needed.
            player.sendInputLine(Constants.MAP_RADIUS+"");
            player.sendInputLine(Constants.CENTER_RADIUS+"");
            player.sendInputLine(Constants.BALL_LOSE_MIN_IMPULSE+"");

            player.sendInputLine(player.Cars.size()+"");
        }

        Player other = game.players.get(1-player.getIndex());
        player.sendInputLine(player.getScore()+"");
        player.sendInputLine(other.getScore()+"");
        player.sendInputLine(getCurrentWinner(player, other)+"");

        int totalSize = player.Cars.size()+other.Cars.size()+game.balls.size();
        player.sendInputLine(totalSize+"");
        for(Car car : player.Cars){
            player.sendInputLine(createCarArgs(car, player));
        }
        for(Car car : other.Cars){
            player.sendInputLine(createCarArgs(car, player));
        }
        for(Ball ball : game.balls){
            player.sendInputLine(createBallArgs(ball));
        }
    }

    private int getCurrentWinner(Player player, Player other){
        if(player.getScore() > other.getScore()){
            return 1;
        }else if(player.getScore() < other.getScore()){
            return -1;
        }

        if(player.getScore() == other.getScore() && player.getScore() > 0){
            if(Math.abs(player.lastScored - other.lastScored) < Constants.EPSILON * 0.1){ // Multiplied by 0.1 in case of times < EPSILON * 10.
                return 0;
            }
            else if(player.lastScored < other.lastScored){
               return 1;
            }
            else {
                return -1;
            }
        }

        return 0;
    }

    private String createCarArgs(Car car, Player player){
        int angle = (int)Math.round(Math.toDegrees(car.angle));
        int type = car.player==player?0:1;
        int ballId = car.ball==null?-1:car.ball.id;
        int[] inputs = {car.id, type, (int)car.x, (int)car.y, (int)car.vx, (int)car.vy, angle, ballId};
        return Arrays.toString(inputs).replaceAll(", ", " ").replace("[", "").replace("]", "");
    }

    private String createBallArgs(Ball ball){
        int[] inputs = {ball.id, 2, (int)ball.x, (int)ball.y, (int)ball.vx, (int)ball.vy, -1, ball.id};
        return Arrays.toString(inputs).replaceAll(", ", " ").replace("[", "").replace("]", "");
    }

    private void handleInput(Car car, String input) throws Exception{
        String[] splitted = input.split(" ");

        if(splitted[0].equals("EXPERT")){
            int angle = Integer.parseInt(splitted[1]);
            int thrust = Integer.parseInt(splitted[2]);
            if(thrust < 0 || thrust > Constants.CAR_MAX_THRUST) {
                gameManager.addToGameSummary("Invalid thrust. Please keep between 0 and " + Constants.CAR_MAX_THRUST);
                throw new Exception( "Invalid thrust");
            }

            if(angle < -18 || angle > 18){
                gameManager.addToGameSummary("Invalid angle. Please keep between -18 and 18.");
                throw new Exception("Invalid angle");
            }

            car.handleExpertInput(angle, thrust);
            if(splitted.length > 3){
                int totalLength = ("EXPERT " +angle+" "+thrust+" ").length();
                car.message = input.substring(totalLength);
                if(car.message.contains("debug")){
                    doDebug = true;
                }

                if(car.message.length() > 20){
                    car.message = car.message.substring(0, 20);
                }
            }else{
                car.message="";
            }

        }
        else{
            int x = Integer.parseInt(splitted[0]);
            int y = Integer.parseInt(splitted[1]);
            int thrust = Integer.parseInt(splitted[2]);
            if(thrust < 0 || thrust > Constants.CAR_MAX_THRUST) {
                gameManager.addToGameSummary("Invalid thrust. Please keep between 0 and " + Constants.CAR_MAX_THRUST);
                throw new Exception( "Invalid thrust");
            }

            car.handleInput(x, y, thrust);
            if(splitted.length > 3){
                int totalLength = (x+" "+y+" "+thrust+" ").length();
                car.message = input.substring(totalLength);
                if(car.message.contains("debug")){
                    doDebug = true;
                }

                if(car.message.length() > 20){
                    car.message = car.message.substring(0, 20);
                }
            }else{
                car.message="";
            }
        }
    }
}
