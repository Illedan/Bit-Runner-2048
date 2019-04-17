package com.codingame.game;

public class Ball extends Unit {
    public boolean captured;

    public Ball(double x, double y){
        super(x,y);
        this.friction = Constants.BALL_FRICTION;
        this.radius = Constants.BALL_RADIUS;
        this.mass = 1;
    }

    @Override
    void reactToCollision(Unit u, Game game) {
        if(this.captured) return; // May only happen in wood.
        if(u==null) {
            this.collisions.add(new UnitCollision(this.clonePoint(), null, game.t, BallState.Unchanged, 0.0));
            bounce(Constants.BORDER_BALL_MIN_IMPULSE);
            return;
        }

        if(((Car)u).ball != null) return; // in case of double balls on equal time.
        this.collisions.add(new UnitCollision(this.clonePoint(), null, game.t, BallState.Stolen, 0.0));
        u.collisions.add(new UnitCollision(u.clonePoint(), null, game.t, BallState.Gained, 0.0));
        captured = true;
        ((Car)u).ball = this;
    }

    public static class Ballswap{
        public Point p1;
        public Point p2;
        public double time;
        public Ballswap(Point p1, Point p2, double time){
            this.p1 = p1;
            this.p2 = p2;
            this.time = time;
        }
    }
}
