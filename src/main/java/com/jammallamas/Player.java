package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {

    public Player() {
        super();
    }

    private byte walking = 0;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return getWalking() == player.getWalking();
    }

    public byte getWalking() {
        return this.walking;
    }

    public void setWalking(byte walking) {
        this.walking = walking;
    }

}
