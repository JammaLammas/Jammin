package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {
    private boolean onGround = false;
    private double yVelocity = 0;

    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX(), getY(), 0);
        glTranslated(-Main.cameraX, -Main.cameraY, 0);
        glBegin(GL_QUADS);
        glColor4f(1, 1, 1, 1);
        glVertex2d(0, 0);
        glVertex2d(0, getHeight());
        glVertex2d(getWidth(), getHeight());
        glVertex2d(getWidth(), 0);
        glEnd();
        glPopMatrix();
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public double getyVelocity() {
        return yVelocity;
    }

    public void setyVelocity(double yVelocity) {
        this.yVelocity = yVelocity;
    }
}
