package messageModule;

import java.util.ArrayList;

import com.codingame.game.Player;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.core.MultiplayerGameManager;

import com.google.inject.Inject;

public class MessageModule implements Module {

    MultiplayerGameManager<Player> gameManager;

    public ArrayList<Integer> messageIds = new ArrayList<>();

    @Inject
    public MessageModule(MultiplayerGameManager<Player> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    @Override
    public void onGameInit() {
        gameManager.setViewGlobalData("message", messageIds.toArray());
    }

    @Override
    public void onAfterGameTurn() {

    }

    @Override
    public void onAfterOnEnd() {

    }

}