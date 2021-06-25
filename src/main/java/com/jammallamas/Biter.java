package com.jammallamas;

public abstract class Biter extends Entity {
    private boolean isChasing = false;
    private final double AGGRO_RANGE = 100;
    private final double MAX_RUN = 20;
    private final double MAX_WALK = 10;
    private final double WALK_ACCEL  = 1;
    private final double CHASE_ACCEL = 3;
    private double accel = WALK_ACCEL;

    private boolean checkForPlayer(Entity player){
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

    public void setChase (Entity player) {
        if (this.checkForPlayer(player)) this.isChasing = true;
        else this.isChasing = false;
    }
}
