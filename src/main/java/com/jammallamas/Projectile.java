package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Projectile extends Entity implements ActionOnTouch {
    private static final long serialVersionUID = 4448422656240288634L;

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

    public boolean onHit(Renderable r) {
        //hit a platform
        return true; //destroy self
    }

    public boolean onHit(Entity e) {
        //hit an entity
        return true; //destroy self
    }
}
