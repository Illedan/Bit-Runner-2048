package com.codingame.game;

import java.util.Random;

public class Constants {

    public static Random Random;

    public static int GLOBAL_ID = 0;

    public static final int MAP_RADIUS = 5000;

    public static final int MAX_GOALS = 10;

    // Ball
    public static final int BALL_SPAWN_RADIUS = 4000;
    public static final int BALL_SPAWN_SPEED = 300;
    public static final double BALL_FRICTION = 0.0;
    public static final int BALL_LOSE_MIN_IMPULSE = 300;
    public static final double BORDER_BALL_MIN_IMPULSE = 42;
    public static final int BALL_RADIUS = 100;
    public static final int NUM_BALLS = 2;

    // Collision
    public static double IMPULSE_COEFF = 1;
    public static final double EPSILON = 0.00001;
    public static final double MIN_IMPULSE = 120;
    public static final double BORDER_MIN_IMPULSE = 600;
    public final static Collision NULL_COLLISION = new Collision(1.0 + EPSILON);

    // Cars
    public static final int CAR_RADIUS = 400;
    public static final double CAR_FRICTION = 0.15;
    public static final double MAX_ROTATION_PER_TURN = Math.PI / 10;
    public static int CAR_MAX_THRUST = 200;
    public static final int MASS = 1;
    public static int NUM_CARS = 2;

    // Center
    public static final int CENTER_RADIUS = 800;
    public static Point CENTER = new Point(0,0);
}
