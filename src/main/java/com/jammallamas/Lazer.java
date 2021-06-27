package com.jammallamas;

import static org.lwjgl.opengl.GL11.glColor4f;

public class Lazer extends Platform implements ActionOnTouch {

    private transient int frameCount = 0;

    public Lazer() {
        super();
    }

    @Override
    public void render() {
        glColor4f(1, 0, 0, 1);
        super.render();
    }

    @Override
    public void onFrame() {
        frameCount++;
        if (frameCount % 300 == 0) {
            visible = !visible;
            collidable = !collidable;
        }
    }

    @Override
    public int getTexture() {
        return 0; //TODO texture ?
    }

    @Override
    public boolean onHit(Entity e) {
        //i don't care who you are, you get deleted
        if (e == Main.player1) {
            Main.queueReset();
        } else if (e == Main.player2) {
            // let's do the grab thingy
            Main.isGrabbed = true;
            //teleport second on top
            Main.player2.setX(Main.player1.getX());
            Main.player2.setY(Main.player1.getY() + Main.player1.getHeight() + 3); // 3 for spacing
            Main.player2.setxVelocity(0);
            Main.player2.setyVelocity(0);
        } else {
            Main.forDeletion.add(e);
        }
        return false;
    }

    @Override
    public boolean onHit(Renderable r) {
        //should never happen
        return false;
    }
}
