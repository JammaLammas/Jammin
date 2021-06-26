package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Biter extends Entity {
    private boolean isChasing = false;
    private final double AGGRO_RANGE = 100;
    private final double MAX_RUN = 20;
    private final double MAX_WALK = 10;
    private final double WALK_ACCEL = 1;
    private final double CHASE_ACCEL = 3;
    private double accel = WALK_ACCEL;

    private boolean checkForPlayer(Entity player) {
        return player.getX() + player.getWidth() > this.getX() - this.AGGRO_RANGE
        && player.getX() < this.getX() + this.getWidth() + this.AGGRO_RANGE
        && player.getX() + player.getHeight() > this.getY() - this.AGGRO_RANGE
        && player.getX() < this.getY() + this.getHeight() + this.AGGRO_RANGE;
    }

    public void setAccel(Entity player){
        if (this.isChasing) this.accel = CHASE_ACCEL;
        else this.accel = WALK_ACCEL;
        if (this.getX() < player.getX()) this.accel *= -1;
        else this.accel = Math.abs(this.accel);
    }

    public void setChase(Entity player) {
        if (this.checkForPlayer(player)) this.isChasing = true;
        else this.isChasing = false;
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
}
