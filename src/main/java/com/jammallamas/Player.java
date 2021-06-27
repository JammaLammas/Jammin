package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {

    private static int p1Texture = 0;
    private static int p2Texture = 0;
    private byte walking = 0;

    public Player() {
        super();
        if (Utils.hasOGLContext()) {
            initTextures();
        }
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX() + getWidth(), getY(), 0);
        glTranslated(-Main.cameraX, -Main.cameraY, 0);
        glScaled(-1, 1, 1);
        glBegin(GL_QUADS);
        glTexCoord2f(1, 1);
        glVertex2d(0, 0);
        glTexCoord2f(1, 0);
        glVertex2d(0, getHeight());
        glTexCoord2f(0, 0);
        glVertex2d(getWidth(), getHeight());
        glTexCoord2f(0, 1);
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

    @Override
    public int getTexture() {
        if (this.equals(Main.player1)) {
            return p1Texture;
        } else {
            return p2Texture;
        }
    }

    @Override
    public void initTextures() {
        if (p1Texture == 0 || p2Texture == 0) {
            try {
                p1Texture = Utils.loadTexture("p1.png");
            } catch (Exception e) {
                e.printStackTrace();
                p1Texture = 0;
            }

            try {
                p2Texture = Utils.loadTexture("p2.png");
            } catch (Exception e) {
                e.printStackTrace();
                p2Texture = 0;
            }
        }
    }
}
