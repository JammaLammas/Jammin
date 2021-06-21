package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Platform extends Renderable {

    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX(), getY(), 0);
        glTranslated(-Main.cameraX, -Main.cameraY, 0);
        glBegin(GL_QUADS);
        glColor4f(0, 0, 0, 1);
        glTexCoord2f(0, 1);
        glVertex2d(0, 0);
        glTexCoord2f(1, 1);
        glVertex2d(0, getHeight());
        glTexCoord2f(0, 0);
        glVertex2d(getWidth(), getHeight());
        glTexCoord2f(1, 0);
        glVertex2d(getWidth(), 0);
        glEnd();
        glPopMatrix();
    }

}
