package com.codingame.game;

import com.codingame.gameengine.module.entities.*;
import debugModule.DebugModule;
import messageModule.MessageModule;
import tooltipModule.TooltipModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewController {

    public static double sizeMulti = 1080.0/(Constants.MAP_RADIUS*2);
    private GraphicEntityModule graphicEntityModule;
    private TooltipModule tooltipModule;
    private MessageModule messageModule;
    private DebugModule debugModule;
    private Group playFieldGroup;
    private Game game;
    public static int X0 = (int)((1920 - 1080/2)/sizeMulti);
    public static int Y0 = Constants.MAP_RADIUS;
    private Map<Unit, ViewPart> sprites = new HashMap<>();
    private ArrayList<ViewPart> undeletableSprites = new ArrayList<>();

    private int getScaled(double val){
            return (int)Math.round(val*sizeMulti);
        }

    public ViewController(GraphicEntityModule graphicEntityModule, TooltipModule tooltipModule, Game game, MessageModule messageModule, DebugModule debugModule){
        this.graphicEntityModule = graphicEntityModule;
        this.tooltipModule = tooltipModule;
        this.messageModule = messageModule;
        this.debugModule = debugModule;
        this.game = game;
    }

    public void Init(){
        graphicEntityModule.createRectangle().setFillColor(0x000000).setWidth(1920).setHeight(1080).setZIndex(-11);
        playFieldGroup = graphicEntityModule.createGroup()
                .setX(getScaled(X0))
                .setY(getScaled(Y0));

        playFieldGroup.add(graphicEntityModule.createSprite().setImage("sand.jpg").setAnchor(0.5).setAlpha(1.0).setZIndex(-10)
                .setTint(0xffffff)
                .setMask(graphicEntityModule.createCircle()
                        .setRadius(getScaled(Constants.MAP_RADIUS)-2)
                        .setLineColor(0x000000)
                        .setFillAlpha(0x000000)
                        .setLineWidth(2)
                        .setFillAlpha(1.0)
                        .setX(getScaled(X0))
                        .setY(getScaled(Y0))));

        playFieldGroup.add(graphicEntityModule.createCircle().setRadius(getScaled(Constants.MAP_RADIUS)).setLineColor(0x000000).setLineWidth(2).setFillAlpha(0.0));

        //playFieldGroup.add(graphicEntityModule.createCircle().setRadius(getScaled(game.center.radius)).setLineColor(0x000000).setLineWidth(0).setFillAlpha(0.2));
        playFieldGroup.add(graphicEntityModule.createSprite().setImage("manhole.png").setBaseHeight(getScaled(game.center.radius*2)).setBaseWidth(getScaled(game.center.radius*2)).setAlpha(0.7).setAnchor(0.5));
        undeletableSprites.add(new ScoringLight());
        undeletableSprites.add(new ElectricFence());
        undeletableSprites.add(new CollisionIndicator());
        undeletableSprites.add(new BallSwap());

        for(Player p : game.players){
            undeletableSprites.add(new PlayerHud(p));
            for(Car c : p.Cars){
                sprites.put(c, new CarSprite(c, p));
            }
        }

        for (Ball b : game.balls){
            sprites.put(b, new BallSprite(b));
        }
    }

    public void update(){
        for (Ball b : game.balls){
            if(sprites.containsKey(b)) continue;
            sprites.put(b, new BallSprite(b));
        }

        for (Map.Entry<Unit, ViewPart> entry : sprites.entrySet())
        {
            entry.getValue().update();
        }

        for(ViewPart sprite : undeletableSprites){
            sprite.update();
        }

        sprites.entrySet().removeIf(e -> e.getValue().isRemoved());
    }

    private String createToolTip(Map<String, Object> params){
        String s = "";
        for(String key : params.keySet()){
            s += key + ":"+ params.get(key)+"\n";
        }

        return s;
    }

    public abstract class ViewPart {
        public abstract void update();
        public abstract boolean isRemoved();
    }

    public class CollisionIndicator extends ViewPart{
        private ArrayList<SpriteAnimation>[] playingAnims;
        private ArrayList<SpriteAnimation> freeAnims = new ArrayList<>();
        public CollisionIndicator(){
            playingAnims = new ArrayList[3];
            for(int i = 0;i < 3; i++){
                playingAnims[i] = new ArrayList<>();
            }

            for(int i = 0; i < 20; i++){
                SpriteAnimation anim = graphicEntityModule.createSpriteAnimation()
                        .setImages("flash00.png", "flash01.png", "flash02.png","flash03.png", "Empty.png")
                        .setX(-1000)
                        .setY(-1000)
                        .setDuration(150)
                        .setLoop(false)
                        .setScale(0.15)
                        .setAnchor(0.5)
                        .setAlpha(0.0, Curve.IMMEDIATE);
                playFieldGroup.add(anim);
                freeAnims.add(anim);
            }
        }
        @Override
        public void update() {
            freeAnims.addAll(playingAnims[0]);
            playingAnims[0].clear();
            ArrayList<SpriteAnimation> temp = playingAnims[0];
            playingAnims[0] = playingAnims[1];
            playingAnims[1] = playingAnims[2];
            playingAnims[2] = temp;

            for(Car c : game.entities){
                for(Unit.UnitCollision col : c.collisions){
                    if(col.targetPosition != null && col.impulse > Constants.MIN_IMPULSE){
                        int color = 0xffffff;
                        if(col.impulse > Constants.BALL_LOSE_MIN_IMPULSE){
                            color = 0xbdb764;
                        }
                        else if(col.impulse > Constants.BALL_LOSE_MIN_IMPULSE-50){
                            color = 0xa49878;
                        }
                        else if(col.impulse > Constants.BALL_LOSE_MIN_IMPULSE-100){
                            color = 0xb3ac98;
                        }

                        double scale = Math.min(1.5,col.impulse/Constants.BALL_LOSE_MIN_IMPULSE) * 0.15;
                        if(freeAnims.size() == 0) break;
                        SpriteAnimation anim = freeAnims.get(freeAnims.size()-1);
                        freeAnims.remove(freeAnims.size()-1);
                        anim.setX(getScaled(col.targetPosition.x), Curve.IMMEDIATE)
                                .setY(getScaled(col.targetPosition.y), Curve.IMMEDIATE)
                                .setTint(color)
                                .setScale(scale, Curve.IMMEDIATE)
                                .setAlpha(1.0, Curve.IMMEDIATE);
                        graphicEntityModule.commitEntityState(Math.min(1.0, Math.max(0.0, col.time-0.001)), anim);

                        anim.reset();
                        anim.play();
                        graphicEntityModule.commitEntityState(Math.min(1.0, col.time), anim);
                        playingAnims[2].add(anim);
                    }
                }
            }
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }

    public class BallSwap extends ViewPart{

        @Override
        public void update() {
            for(Ball.Ballswap b : game.ballswaps){
                double angle = b.p1.getAngle(b.p2);
                double length = b.p1.distance(b.p2);
                int height = 64;
                double scale = (length/height);
                SpriteAnimation anim =
                        graphicEntityModule.createSpriteAnimation()
                                .setImages("ballswap1.png", "ballswap2.png", "ballswap1.png", "Empty.png")
                                .setZIndex(15000)
                                .setRotation(angle-Math.PI*0.5)
                                .setScale(scale * sizeMulti)
                                .setX(getScaled(b.p1.x))
                                .setY(getScaled(b.p1.y))
                                .setDuration(100)
                                .setLoop(false)
                                .setAlpha(1)
                                //.setTint(0xb84e11)
                                .setAnchorX(0.5);
                playFieldGroup.add(anim);
                graphicEntityModule.commitEntityState(Math.max(0.0, Math.min(1.0, b.time)-0.0001), playFieldGroup, anim);
                anim.play();
                graphicEntityModule.commitEntityState(Math.min(1.0, b.time), anim);
            }
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }

    public class ElectricFence extends ViewPart{
        private Group fenceGroup;

        public ElectricFence(){
            fenceGroup = graphicEntityModule.createGroup()
                    .setZIndex(3)
                    .setX(getScaled(X0))
                    .setY(getScaled(Y0));
            int count = 20;
            for(int i = 0; i < count; i++){
                double angle = Math.PI*2/count*i;
                Group dummy = graphicEntityModule.createGroup().setRotation(angle);
                fenceGroup.add(dummy);
                dummy.add(graphicEntityModule.createSpriteAnimation()
                        .setImages("Elec1.png", "Elec2.png", "Elec3.png")
                        .setX(-295)
                        .setY(-320)
                        .setDuration(250+i)
                        .setLoop(true)
                        .setAlpha(1)
                        .setAnchor(1)
                        .play());
            }
        }

        @Override
        public void update() {
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }

    public class ScoringLight extends ViewPart{
        private int p1Score, p2Score;
        private ArrayList<Circle> p1Lights = new ArrayList();
        private ArrayList<Circle> p2Lights = new ArrayList<>();
        public ScoringLight(){
            double lightradius = getScaled(4500);
            for(int i = 9+90 ; i < 175+90; i+= 18){
                double radians = Math.toRadians(i);
                double dx = Math.cos(radians) * lightradius;
                double dy = Math.sin(radians)* lightradius;
                Circle circle = graphicEntityModule.createCircle().setFillAlpha(0.5).setLineAlpha(0.5).setRadius(25).setFillColor(0xffffff).setLineColor(0x000000).setLineWidth(4).setX((int)dx).setY((int)dy).setZIndex(-2);

                p1Lights.add(circle);
                playFieldGroup.add(circle);
            }

            for(int i = 351+90 ; i >= 185+90; i-= 18){
                double radians = Math.toRadians(i);
                double dx = Math.cos(radians) * lightradius;
                double dy = Math.sin(radians)* lightradius;
                Circle circle = graphicEntityModule.createCircle().setFillAlpha(0.5).setLineAlpha(0.5).setRadius(25).setFillColor(0xffffff).setLineColor(0x000000).setLineWidth(4).setX((int)dx).setY((int)dy).setZIndex(-2);
                p2Lights.add(circle);
                playFieldGroup.add(circle);
            }
        }

        @Override
        public void update() {
            if(game.players.get(0).getScore() != p1Score){
                for(int i = p1Score; i < game.players.get(0).getScore(); i++){
                    if(i < p1Lights.size()) {
                        Circle light = p1Lights.get(i);
                        light.setFillColor(game.players.get(0).getColorToken(), Curve.NONE);
                        playFieldGroup.add(graphicEntityModule.createSprite().setImage("prisoner.png")
                                .setBaseHeight(50).setBaseWidth(70).setAnchor(0.5).setX(light.getX()).setY(light.getY()).setZIndex(-1)
                                .setMask(graphicEntityModule.createCircle().setFillAlpha(1).setRadius(25).setFillColor(0xffffff).setX(light.getX() + playFieldGroup.getX()).setY(light.getY() + playFieldGroup.getY())));
                    }
                }
            }

            if(game.players.get(1).getScore() != p2Score){
                for(int i = p2Score; i < game.players.get(1).getScore(); i++){
                    if(i < p2Lights.size()) {
                        Circle light = p2Lights.get(i);
                        light.setFillColor(game.players.get(1).getColorToken(), Curve.NONE);
                        playFieldGroup.add(graphicEntityModule.createSprite().setImage("prisoner.png")
                                .setBaseHeight(50).setBaseWidth(70).setAnchor(0.5).setX(light.getX()).setY(light.getY()).setZIndex(-1)
                                .setMask(graphicEntityModule.createCircle().setFillAlpha(1).setRadius(25).setFillColor(0xffffff).setX(light.getX() + playFieldGroup.getX()).setY(light.getY() + playFieldGroup.getY())));
                    }
                }
            }

            p1Score = game.players.get(0).getScore();
            p2Score = game.players.get(1).getScore();
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }

    public class CarSprite extends ViewPart
    {
        private Sprite arrow;
        private Sprite car;
        private Car model;
        private Group cargroup;
        private Group rotationGroup;
        private Text message;
        private Group ballGroup;
        private Circle previousLocation;
        private boolean first = true;

        public CarSprite(Car model, Player player){
            this.model = model;
            playFieldGroup.add(previousLocation = graphicEntityModule.createCircle()
                    .setRadius(getScaled(model.radius))
                    .setFillAlpha(0.0)
                    .setX(getScaled(model.x))
                    .setY(getScaled(model.y))
                    .setLineColor(player.getColorToken())
                    .setLineAlpha(0.1)
                    .setAlpha(0.5)
                    .setLineWidth(5).setVisible(false));
            debugModule.addItem(previousLocation.getId());

            playFieldGroup.add(cargroup =
                    graphicEntityModule.createGroup()
                    .setX(getScaled(model.x))
                    .setY(getScaled(model.y)).setZIndex(4));
            cargroup.add(rotationGroup = graphicEntityModule.createGroup());

          //  rotationGroup.add(graphicEntityModule.createSpriteAnimation()
          //          .setImages("Smoke5.png", "Smoke3.png")
          //          .setDuration(100)
          //          .setLoop(true)
          //          .setScale(1)
          //          .setAnchor(0.5)
          //          .setAlpha(0.4).setY(-45)
          //          .play());

            cargroup.add(message = graphicEntityModule
                    .createText("")
                    .setStrokeThickness(2)
                    .setStrokeColor(0x000000)
                    .setFillColor(player.getColorToken())
                    .setAnchorX(0.5)
                    .setZIndex(5)
                    .setAnchorY(1.0)
                    .setY(getScaled(model.radius*-1)));

            rotationGroup.add(graphicEntityModule.createCircle().setAlpha(0.2)
                    .setRadius(getScaled(model.radius))
                    .setFillColor(player.getColorToken())
                    .setLineColor(player.getColorToken()).setLineWidth(2));
            rotationGroup.setRotation(model.angle - Math.PI/2);

            rotationGroup.add(graphicEntityModule.createLine()
                    .setX(0)
                    .setX2(0).setY(0).setY2(getScaled(model.radius)).setAlpha(0.5).setLineColor(0xffffff).setLineWidth(5));

            Map<String, Object> params = new HashMap<>();
            params.put("Id", model.id);
            params.put("Owner", player.getNicknameToken());
            tooltipModule.registerEntity(cargroup, params);

           rotationGroup.add(this.car = graphicEntityModule.createSprite()
                 .setImage("car"+model.player.getIndex()+".png")
                 .setAnchor(0.5)
                 .setBaseWidth(getScaled(model.radius*2))
                 .setBaseHeight(getScaled(model.radius*1.3))
                 .setAlpha(1.0)
                 .setTint(model.player.getColorToken())
                 .setRotation(Math.PI*0.5));

           cargroup.add(arrow = graphicEntityModule.createSprite()
                   .setImage("Arrow.png")
                   .setAnchorY(0.5)
                   .setAnchorX(0)
                   //.setBaseWidth(getScaled(model.radius*2))
                   //.setBaseHeight(getScaled(model.radius*1.3))
                   .setAlpha(1.0)
                   .setScale(0.5)
                   .setTint(0xf5ee00).setVisible(false)
                   .setRotation(Math.PI*0.5));

           debugModule.addItem(arrow.getId());
            messageModule.messageIds.add(message.getId());

            // ball
            rotationGroup.add(ballGroup = graphicEntityModule.createGroup().setAlpha(0.0).setY(-25));
            ballGroup.add(graphicEntityModule.createSpriteAnimation()
                    .setImages("fx01.png", "fx02.png", "fx03.png", "fx04.png", "fx05.png", "fx06.png", "fx07.png", "fx08.png", "fx09.png")
                    .setDuration(800)
                    .setLoop(true)
                    .setScale(0.3)
                    .setAnchor(0.5)
                    .setAlpha(0.7)
                    .play());

            ballGroup.add(graphicEntityModule.createSprite().setImage("ball4.png").setScale(0.7).setAnchor(0.5).setAlpha(1));
            Text idText = graphicEntityModule.createText(""+model.id).setFillColor(0xffffff).setStrokeColor(0x000000).setStrokeThickness(1).setAnchor(0.5).setFontSize(25).setVisible(false);
            cargroup.add(idText);
            debugModule.addItem(idText.getId());
        }

        @Override
        public void update(){
            previousLocation.setX(cargroup.getX(), Curve.IMMEDIATE).setY(cargroup.getY(), Curve.IMMEDIATE);

            double angle = model.angle;
            if(first){
                rotationGroup.setRotation(angle - Math.PI/2);
                graphicEntityModule.commitEntityState(0, rotationGroup);
                first = false;
            }

            double t = 0.0;
            for (Unit.UnitCollision c : model.collisions){
                if(Math.abs(t-c.time) > Constants.EPSILON){
                    int newX = getScaled(c.point.x);
                    int newY = getScaled(c.point.y);
                    cargroup.setX(newX)
                            .setY(newY);
                    graphicEntityModule.commitEntityState(Math.min(1, c.time), cargroup);
                }

                if (c.ballState != Unit.BallState.Unchanged){
                    double alpha = c.ballState == Unit.BallState.Gained ? 1.0 : 0.0;
                    //if(Math.abs(ballSprite.getAlpha()-alpha) > 0.1)
                    {
                        ballGroup.setAlpha(alpha, Curve.NONE);
                        graphicEntityModule.commitEntityState(Math.min(1, c.time), ballGroup);
                    }
                }

                t = c.time;
            }

            arrow.setRotation(model.getSpeedAngle(), Curve.NONE);
            arrow.setScale(0.2+0.25*model.getSpeed()/500);
            cargroup.setX(getScaled(model.x))
                    .setY(getScaled(model.y))
                    .setZIndex((int)model.y+Constants.MAP_RADIUS); // keep message above

            rotationGroup.setRotation(angle - Math.PI/2);

           // double alpha = model.ball == null ? 0.0 : 1.0;
           // if(Math.abs(ballSprite.getAlpha()-alpha) > 0.1)
           //     ballSprite.setAlpha(alpha);

            if(model.message != message.getText()){
                message.setText(model.message == null ? "" : model.message);
                graphicEntityModule.commitEntityState(0.0, message);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("CarX", model.x);
            params.put("CarY", model.y);
            params.put("Vx", model.vx);
            params.put("Vy", model.vy);
            params.put("Angle", Math.round(Math.toDegrees(model.angle)));
            if(model.ball != null){
                params.put("PrisonerId", model.ball.id);
            }
            tooltipModule.updateExtraTooltipText(cargroup, createToolTip(params));
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }

    public class BallSprite extends ViewPart {
        private Ball model;
        private Group ballGroup;
        private Group rotationGroup;
        private SpriteAnimation ball;
        public BallSprite(Ball model){
            this.model = model;
            playFieldGroup.add(ballGroup = graphicEntityModule.createGroup().setZIndex(2).setX(getScaled(model.x)).setY(getScaled(model.y)));
            ballGroup.add(graphicEntityModule.createCircle().setRadius((int)(50*0.7/2)).setLineColor(0x000000).setLineWidth(3).setFillAlpha(0.5).setLineAlpha(0.5).setFillColor(0xffffff));
            ballGroup.add(rotationGroup = graphicEntityModule.createGroup());
            rotationGroup.add(ball = graphicEntityModule.createSpriteAnimation().setImages( "ball6.png", "ball5.png", "ball4.png").setDuration(400).setLoop(true).setScale(0.7).setAnchor(0.5).play());
            Map<String, Object> params = new HashMap<>();
            params.put("Id", model.id);
            tooltipModule.registerEntity(ballGroup, params);

            Text idText = graphicEntityModule.createText(""+model.id).setStrokeColor(0x000000).setStrokeThickness(1).setFillColor(0xffffff).setAnchor(0.5).setFontSize(25).setVisible(false);
            ballGroup.add(idText);
            debugModule.addItem(idText.getId());
        }

        @Override
        public void update() {
            for (Unit.UnitCollision c : model.collisions){
                ballGroup.setX(getScaled(c.point.x))
                        .setY(getScaled(c.point.y));
                graphicEntityModule.commitEntityState(Math.min(1, c.time), ballGroup);

                if(c.ballState == Unit.BallState.Stolen){
                    ballGroup.setAlpha(0.0, Curve.NONE);
                    graphicEntityModule.commitEntityState(Math.min(1, c.time), ballGroup);
                }
            }
            if(!model.captured){
                ballGroup.setX(getScaled(model.x)).setY(getScaled(model.y));
                rotationGroup.setRotation(Math.atan2(model.vy, model.vx) - Math.PI*1.5);

                Map<String, Object> params = new HashMap<>();
                params.put("ballX", model.x);
                params.put("ballY", model.y);
                params.put("Vx", model.vx);
                params.put("Vy", model.vy);
                tooltipModule.updateExtraTooltipText(ballGroup, createToolTip(params));
            }
        }

        @Override
        public boolean isRemoved() {
            return model.captured;
        }
    }

    public class PlayerHud extends ViewPart {
        private Player model;
        private Group playerGroup;
        private BitmapText scoreText;
        private int lastScore;
        public PlayerHud(Player model){
            this.model = model;
            int centerY = 1080/2;
            int y = centerY -(model.getIndex()==0? centerY/2 : (centerY/-   2));

            playerGroup = graphicEntityModule.createGroup().setX(420).setY(y);

            //Nick
            playerGroup.add(graphicEntityModule.createBitmapText().setText(model.getNicknameToken()).setFont("font")
                    .setAnchorX(0.5).setAnchorY(1).setY(-120).setX(0).setFontSize(50));
            playerGroup.add(graphicEntityModule.createLine().setX(-250).setX2(250).setY(-115).setY2(-115).setLineColor(model.getColorToken()).setLineWidth(10));

            //Avatar
            playerGroup.add(graphicEntityModule.createSprite()
                    .setImage(model.getAvatarToken()).setAnchor(0.5).setBaseHeight(200).setBaseWidth(200).setY(50).setX(-125));

            //Frame
            playerGroup.add(graphicEntityModule.createRectangle().setWidth(200).setHeight(200).setX(-225)
                    .setY(-50).setLineWidth(10).setFillAlpha(0.0).setLineColor(model.getColorToken()));

            //Score
            playerGroup.add(scoreText = graphicEntityModule
                    .createBitmapText()
                    .setFont("font")
                    .setText("0")
                    .setFontSize(130)
                    .setAnchor(0.5)
                    .setY(50).setX(125));
        }

        @Override
        public void update() {
            int score = model.getScore();

            if(score != lastScore){
                scoreText.setText(model.getScore()+"");
                lastScore = score;
            }
        }

        @Override
        public boolean isRemoved() {
            return false;
        }
    }
}
