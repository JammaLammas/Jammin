package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class FinalDoor extends Renderable implements ActionOnTouch {

    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX() + getWidth(), getY(), 0);
        glTranslated(-Main.cameraX, -Main.cameraY, 0);
        glScaled(-1, 1, 1);
        glBegin(GL_QUADS);
        glColor4f(1, 1, 1, 1);
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
    public boolean onHit(Entity e) {
        if (!Main.isLoading) {
            Main.isLoading = true;
            System.out.println("more levels !");
            Main.currentLevel++;
            Main.queueReset();
        }
        return false;
    }

    @Override
    public boolean onHit(Renderable r) {
        //never called
        return false;
    }
}
