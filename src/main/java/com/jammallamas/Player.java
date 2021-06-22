package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {
	private byte walking = 0;

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

	
    public byte getWalking() {
        return this.walking;
    }

    public void setWalking(byte walking) {
        this.walking = walking;
    }

}
