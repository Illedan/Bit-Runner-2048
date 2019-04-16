package com.codingame.game;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

import java.util.ArrayList;

// Uncomment the line below and comment the line under it to create a Solo Game
// public class Player extends AbstractSoloPlayer {
public class Player extends AbstractMultiplayerPlayer {
    public ArrayList<Car> Cars = new ArrayList<>();
    public double lastScored;
    @Override
    public int getExpectedOutputLines() {
        return Cars.size();
    }

    public void initialize(int carCount, int dx, int starty){
        double x = 2500 * dx;
        int playerDirection = getIndex()==0?-1:1;
        for(int i = 0; i < carCount; i++)
        {
            Car newCar = new Car(this, x, (starty-i*starty*2)*playerDirection);
            Cars.add(newCar);
        }
    }

    public void score(double time){
        this.setScore(this.getScore()+1);
        lastScored = time;
    }

    @Override
    public int hashCode() {
        return getIndex()+100000;
    }
}
