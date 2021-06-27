package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Platform extends Renderable {

    private static int staticTexture = 0;

    public Platform() {
        super();
        if (staticTexture == 0) {
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


    @Override
    public void initTextures() {
        try {
            staticTexture = Utils.loadTexture("platform.png");
        } catch (Exception e) {
            e.printStackTrace();
            staticTexture = 0;
        }
    }

    @Override
    public int getTexture() {
        return staticTexture;
    }
}
