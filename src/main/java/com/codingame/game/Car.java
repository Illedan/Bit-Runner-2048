package com.codingame.game;

public class Car extends Unit {
    public Player player;
    public Double angle;
    public Ball ball;
    public String message;

    Car(Player player, double x, double y) {
        super(x, y);
        this.mass = Constants.MASS;
        this.radius = Constants.CAR_RADIUS;
        this.player = player;
        this.friction = Constants.CAR_FRICTION;
    }

    public void handleExpertInput(int angle, int thrust){
        if(this.angle==null) this.angle = 0.0;

        double newAngle = Math.toDegrees(this.angle) + angle;
        this.angle = Math.toRadians(newAngle);
        thrustTowardsHeading(thrust);
    }

    public void handleInput(int x, int y, int thrust){
        if(angle == null){
            this.thrust(new Point(x, y), thrust);
            if(this.x != x || this.y != y)
                this.angle = getAngle(new Point(x, y));
            else this.angle = 0.0;
        }
        else if (this.x != x || this.y != y) {
            double angle = this.getAngle(new Point(x, y));
            double relativeAngle = shortAngleDist(this.angle, angle);
            if (Math.abs(relativeAngle) >= Constants.MAX_ROTATION_PER_TURN) {
                angle = this.angle + Constants.MAX_ROTATION_PER_TURN * Math.signum(relativeAngle);
            }

            this.angle = angle;
            thrustTowardsHeading(thrust);
        }
    }

    private void thrustTowardsHeading(int thrust){
        double vx = Math.cos(angle) * thrust;
        double vy = Math.sin(angle) * thrust;

        this.vx += vx;
        this.vy += vy;
    }

    @Override
    public void adjust() {
        super.adjust();
        if(this.angle != null){
            double degrees = Math.round(Math.toDegrees(angle));
            this.angle = Math.toRadians(degrees);
            while(this.angle > Math.PI*2) this.angle-= Math.PI*2;
            while(this.angle < 0)this.angle+= Math.PI*2;
        }
    }

    private static double shortAngleDist(double a0, double a1) {
        double max = Math.PI * 2;
        double da = (a1 - a0) % max;
        return 2 * da % max - da;
    }


    @Override
    void reactToCollision(Unit u, Game game) {
        if(u == null) {
            double impulse = this.bounce(Constants.BORDER_MIN_IMPULSE);
            this.collisions.add(new UnitCollision(this.clonePoint(), getPoint(Constants.CENTER, -radius), game.t, BallState.Unchanged, impulse));
        }
        else {
            Car other = (Car)u;
            double impulse = this.bounce(other);
            if(this.ball != null)
                this.collisions.add(new UnitCollision(this.clonePoint(), getPoint(other, radius), game.t, BallState.Gained, impulse));
            else
                this.collisions.add(new UnitCollision(this.clonePoint(), getPoint(other, radius), game.t, BallState.Stolen, impulse));

            if(other.ball != null)
                other.collisions.add(new UnitCollision(other.clonePoint(), null, game.t, BallState.Gained, impulse));
            else
                other.collisions.add(new UnitCollision(other.clonePoint(), null, game.t, BallState.Stolen, impulse));
        }
    }
}