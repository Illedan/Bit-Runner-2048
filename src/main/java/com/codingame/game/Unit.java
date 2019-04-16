package com.codingame.game;

import java.util.ArrayList;

public abstract class Unit extends Point {
    int id;
    public double vx;
    public double vy;
    public double beforeColvx, beforeColvy;
    public double radius;
    public double mass;
    public double friction;
    public ArrayList<UnitCollision> collisions = new ArrayList<>();

    Unit(double x, double y) {
        super(x, y);

        id = Constants.GLOBAL_ID++;

        vx = 0.0;
        vy = 0.0;
    }

    @Override
    public int hashCode() {
        return id;
    }

    void move(double t) {
        x += vx * t;
        y += vy * t;
    }

    void beforeRound(){
        collisions.clear();
    }

    public void thrust(Point p, int power) {
        double distance = distance(p);

        // Avoid a division by zero
        if (Math.abs(distance) <= Constants.EPSILON) {
            return;
        }

        double coef = (((double) power) / mass) / distance;
        vx += (p.x - this.x) * coef;
        vy += (p.y - this.y) * coef;
    }


    public void adjust() {
        x = Utility.truncate(x);
        y = Utility.truncate(y);

        vx = Utility.truncate(vx * (1.0 - friction));
        vy = Utility.truncate(vy * (1.0 - friction));
    }

    // Search the next collision with the map border
    Collision getCollision() {
        // Check instant collision
        if (distance(Constants.CENTER) + radius >= Constants.MAP_RADIUS) {
            return new Collision(0.0, this);
        }

        // We are not moving, we can't reach the map border
        if (vx == 0.0 && vy == 0.0) {
            return Constants.NULL_COLLISION;
        }

        double a = vx * vx + vy * vy;

        if (a <= 0.0) {
            return Constants.NULL_COLLISION;
        }

        double b = 2.0 * (x * vx + y * vy);
        double c = x * x + y * y - (Constants.MAP_RADIUS - radius) * (Constants.MAP_RADIUS - radius);
        double delta = b * b - 4.0 * a * c;

        if (delta <= 0.0) {
            return Constants.NULL_COLLISION;
        }

        double t = (-b + Math.sqrt(delta)) / (2.0 * a);

        if (t <= 0.0) {
            return Constants.NULL_COLLISION;
        }

        return new Collision(t, this);
    }

    // Search the next collision with an other unit
    Collision getCollision(Unit u) {
        return getCollision(u, radius+u.radius);
    }

    Collision getCollision(Unit u, double checkedRadius) {
        // Check instant collision
        if (distance(u) <= checkedRadius) {
            return new Collision(0.0, this, u);
        }

        // Both units are motionless
        if (vx == 0.0 && vy == 0.0 && u.vx == 0.0 && u.vy == 0.0) {
            return Constants.NULL_COLLISION;
        }

        // Change referencial
        // Unit u is not at point (0, 0) with a speed vector of (0, 0)
        double x2 = x - u.x;
        double y2 = y - u.y;
        double r2 = checkedRadius;
        double vx2 = vx - u.vx;
        double vy2 = vy - u.vy;

        double a = vx2 * vx2 + vy2 * vy2;

        if (a <= 0.0) {
            return Constants.NULL_COLLISION;
        }

        double b = 2.0 * (x2 * vx2 + y2 * vy2);
        double c = x2 * x2 + y2 * y2 - r2 * r2;
        double delta = b * b - 4.0 * a * c;

        if (delta < 0.0) {
            return Constants.NULL_COLLISION;
        }

        double t = (-b - Math.sqrt(delta)) / (2.0 * a);

        if (t <= 0.0) {
            return Constants.NULL_COLLISION;
        }

        return new Collision(t, this, u);
    }


    abstract void reactToCollision(Unit u, Game game);

        // Bounce between 2 units
    double bounce(Car u) {
        double mcoeff = (mass + u.mass) / (mass * u.mass);
        double nx = x - u.x;
        double ny = y - u.y;
        double nxnysquare = nx * nx + ny * ny;
        double dvx = beforeColvx - u.beforeColvx;
        double dvy = beforeColvy - u.beforeColvy;
        double product = (nx * dvx + ny * dvy) / (nxnysquare * mcoeff);
        double fx = nx * product;
        double fy = ny * product;
        double m1c = 1.0 / mass;
        double m2c = 1.0 / u.mass;

        vx -= fx * m1c;
        vy -= fy * m1c;
        u.vx += fx * m2c;
        u.vy += fy * m2c;

        fx = fx * Constants.IMPULSE_COEFF;
        fy = fy * Constants.IMPULSE_COEFF;

        // Normalize vector at min or max impulse
        double impulse = Math.sqrt(fx * fx + fy * fy);

        // Swapping balls
        Car other = ((Car)this);
        if(u.ball != null || other.ball != null){
            if(impulse > Constants.BALL_LOSE_MIN_IMPULSE){
                Ball temp = u.ball;
                u.ball = other.ball;
                other.ball = temp;
            }
        }

        double coeff = 1.0;
        if (impulse > Constants.EPSILON && impulse < Constants.MIN_IMPULSE) {
            coeff = Constants.MIN_IMPULSE / impulse;
            impulse = Constants.MIN_IMPULSE;
        }

        fx = fx * coeff;
        fy = fy * coeff;

        vx -= fx * m1c;
        vy -= fy * m1c;
        u.vx += fx * m2c;
        u.vy += fy * m2c;

        double diff = (distance(u) - radius - u.radius) / 2.0;
        if (diff <= 0.0) {
            // Unit overlapping. Fix positions.
            moveTo(u, diff - Constants.EPSILON);
            u.moveTo(this, diff - Constants.EPSILON);
        }

        return impulse;
    }

    // Bounce with the map border
    double bounce(double minImpulse) {
        double mcoeff = 1.0 / mass;
        double nxnysquare = x * x + y * y;
        double product = (x * beforeColvx + y * beforeColvy) / (nxnysquare * mcoeff);
        double fx = x * product;
        double fy = y * product;

        vx -= fx * mcoeff;
        vy -= fy * mcoeff;

        fx = fx * Constants.IMPULSE_COEFF;
        fy = fy * Constants.IMPULSE_COEFF;

        // Normalize vector at min or max impulse
        double impulse = Math.sqrt(fx * fx + fy * fy);
        double coeff = 1.0;
        if (impulse > Constants.EPSILON && impulse < minImpulse) {
            coeff = minImpulse / impulse;
            impulse = minImpulse;
        }

        fx = fx * coeff;
        fy = fy * coeff;
        vx -= fx * mcoeff;
        vy -= fy * mcoeff;

        double diff = distance(Constants.CENTER) + radius - Constants.MAP_RADIUS;
        if (diff >= 0.0) {
            // Unit still outside of the map, reposition it
            moveTo(Constants.CENTER, diff + Constants.EPSILON);
        }

        return impulse;
    }

    public enum BallState{
      Unchanged, Gained, Stolen
    }

    public static class UnitCollision{
        public Point point;
        public Point targetPosition;
        public double time;
        public BallState ballState;
        public double impulse;

        public UnitCollision(Point point, Point targetPosition, double time, BallState ballState, double impulse){
            this.point = point;
            this.time = time;
            this.ballState = ballState;
            this.targetPosition = targetPosition;
            this.impulse = impulse;
        }
    }
}