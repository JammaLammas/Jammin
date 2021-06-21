package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {
    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX(), getY(), 0);
        glBegin(GL_QUADS);
        glColor4f(1, 1, 1, 1);
        glVertex2d(0, 0);
        glVertex2d(0, getHeight());
        glVertex2d(getWidth(), getHeight());
        glVertex2d(getWidth(), 0);
        glEnd();
        glPopMatrix();
    }
}
