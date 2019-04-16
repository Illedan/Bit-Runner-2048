package com.codingame.game;

public class Center extends Unit {
    public Center() {
        super(0, 0);
        this.radius = Constants.CENTER_RADIUS;
        this.friction = 1.0;
        this.mass = 0;
    }

    public Ball getBall(Unit spawner)  {
        double spawnDist = spawner.distance(this);
        double ballX = 0;
        double ballY = Constants.BALL_SPAWN_RADIUS;
        if(Math.abs(spawnDist) > Constants.EPSILON) {
            ballX = spawner.x / spawnDist * (Constants.BALL_SPAWN_RADIUS);
            ballY = spawner.y / spawnDist * (Constants.BALL_SPAWN_RADIUS);
        }

        Ball ball = new Ball(ballX, ballY);
        ball.thrust(new Point(ball.x-spawner.beforeColvx, ball.y-spawner.beforeColvy), Constants.BALL_SPAWN_SPEED);
        return ball;
    }

    @Override
    void reactToCollision(Unit u, Game game) {
        Car car = (Car)u;
        if (car == null || car.ball == null) return; //only happends in same time collisions
        game.score(car.player.getIndex());
        car.ball=null;
        game.addBall(getBall(u));

        // View
        u.collisions.add(new UnitCollision(u.clonePoint(), null, game.t, BallState.Stolen, 0.0));
    }

    Collision getCarCollision(Car car){
        if(car.ball == null) return Constants.NULL_COLLISION;
        return getCollision(car, this.radius);
    }
}
