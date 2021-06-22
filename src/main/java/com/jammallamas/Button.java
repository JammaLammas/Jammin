package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Button extends Renderable implements ActionOnTouch {

    private ButtonLinkable action = null;


    @Override
    public void render() {
        glPushMatrix();
        glTranslated(getX(), getY(), 0);
        glTranslated(-Main.cameraX, -Main.cameraY, 0);
        glBegin(GL_QUADS);
        glColor4f(1, 1, 1, 1);
        glTexCoord2f(0, 0);
        glVertex2d(0, 0);
        glTexCoord2f(0, 1);
        glVertex2d(0, getHeight());
        glTexCoord2f(1, 1);
        glVertex2d(getWidth(), getHeight());
        glTexCoord2f(1, 0);
        glVertex2d(getWidth(), 0);
        glEnd();
        glPopMatrix();
    }


    @Override
    public boolean onHit(Entity e) {
        if (e instanceof Projectile && action != null) {
            action.onButton();
        }
        return false;
    }

    @Override
    public boolean onHit(Renderable r) {
        return false;
    }

    public void setAction(ButtonLinkable action) {
        this.action = action;
    }
}
