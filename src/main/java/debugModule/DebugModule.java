package debugModule;

import java.util.ArrayList;

import com.codingame.game.Player;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.core.MultiplayerGameManager;

import com.google.inject.Inject;

public class DebugModule implements Module {

    MultiplayerGameManager<Player> gameManager;

    private ArrayList<Integer> debugItems = new ArrayList<>();
    private ArrayList<Integer> newItems = new ArrayList<>();
    @Inject
    public DebugModule(MultiplayerGameManager<Player> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    @Override
    public void onGameInit() {
        sendItems();
    }

    @Override
    public void onAfterGameTurn() {
        sendItems();
    }

    @Override
    public void onAfterOnEnd() {
        sendItems();
    }

    private void sendItems(){
        gameManager.setViewData("debug", newItems.toArray());
        debugItems.addAll(newItems);
        newItems.clear();
    }

    public void addItem(int id){
        if(debugItems.contains(id)) return;
        newItems.add(id);
    }
}