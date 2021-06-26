package com.jammallamas;

import static org.lwjgl.opengl.GL11.*;

public class Biter extends Entity implements ActionOnTouch {
    private final double AGGRO_RANGE = 350;
    private final double MAX_RUN = 20;
    private final double MAX_WALK = 10;
    private final double WALK_ACCEL = 1;
    private final double CHASE_ACCEL = 4;
    private boolean isChasing = false;
    private double accel = WALK_ACCEL;
    private boolean dropsEdges = false;

    private boolean checkForPlayer(Entity player) {
        return player.getX() + player.getWidth() >= this.getX() - this.AGGRO_RANGE
                && player.getX() <= this.getX() + this.getWidth() + this.AGGRO_RANGE
                && player.getY() + player.getHeight() > this.getY() - this.AGGRO_RANGE
                && player.getY() < this.getY() + this.getHeight() + this.AGGRO_RANGE;
    }

    public void setChase(Entity player) {
        this.isChasing = this.checkForPlayer(player);
    }

    public boolean isChasing() {
        return isChasing;
    }

    public double getAccel() {
        return accel;
    }

    public void setAccel(Entity player) {
        if (this.isChasing) this.accel = CHASE_ACCEL;
        else this.accel = WALK_ACCEL;
        if (this.getX() > player.getX()) this.accel *= -1;
        else this.accel = Math.abs(this.accel);
        if (Main.getPlatformAt(getX() + this.accel, getY() - 1) == null) {
            //uh uh
            if (!dropsEdges) {
                this.accel = 0; //let's not jump off
            }
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
    public boolean onHit(Entity e) {
        if (e == Main.player1) {
            Main.queueReset(); //reset ?
        } else if (e == Main.player2) {
            //TODO maybe change this ?
            //reset to player1
            Main.isGrabbed = true;
            //teleport second on top
            Main.player2.setX(Main.player1.getX());
            Main.player2.setY(Main.player1.getY() + Main.player1.getHeight() + 3); // 3 for spacing
            Main.player2.setxVelocity(0);
            Main.player2.setyVelocity(0);
        }
        return e instanceof Projectile; //if it's a projectile, kill the biter
    }

    @Override
    public boolean onHit(Renderable r) {
        return false;
    }

    public boolean isDropsEdges() {
        return dropsEdges;
    }

    public void setDropsEdges(boolean dropsEdges) {
        this.dropsEdges = dropsEdges;
    }
}
