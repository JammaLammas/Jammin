package com.jammallamas;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Button extends Renderable implements ActionOnTouch {

    private static int buttonTexture = 0;
    private ArrayList<ButtonLinkable> action = new ArrayList<>();

    public Button() {
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
    public void initTextures() {
        if (buttonTexture == 0) {
            super.initTextures(); //call needed for all platforms
            try {
                buttonTexture = Utils.loadTexture("button.png");
            } catch (Exception e) {
                e.printStackTrace();
                buttonTexture = 0;
            }
        }
    }

    @Override
    public int getTexture() {
        return buttonTexture;
    }

    private transient long cooldown = 0;

    @Override
    public boolean onHit(Entity e) {
        //if (e instanceof Projectile) {
        if (cooldown <= System.currentTimeMillis()) {
            cooldown = System.currentTimeMillis() + 1000;
            for (ButtonLinkable act : action) {
                act.onButton();
            }
        }
        //}
        return false;
    }

    @Override
    public boolean onHit(Renderable r) {
        return false;
    }

    public void addAction(ButtonLinkable action) {
        this.action.add(action);
    }
}
