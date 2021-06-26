package com.jammallamas;

public class BouncyPlatform extends Platform implements ActionOnTouch {
    @Override
    public boolean onHit(Entity e) {
        e.setyVelocity(-e.getyVelocity());
        e.setY(e.getY() + Math.signum(e.getyVelocity()));
        e.setOnGround(true);
        return false;
    }

    @Override
    public boolean onHit(Renderable r) {
        return false;
    }
}
