package com.jammallamas;

public class BouncyPlatform extends Platform implements ActionOnTouch {

    private static int bounceTexture = 0;

    public BouncyPlatform() {
        super();
        if (bounceTexture == 0) {
            initTextures();
        }
    }

    @Override
    public void initTextures() {
        super.initTextures(); //call needed for all platforms
        try {
            bounceTexture = Utils.loadTexture("spring.png");
        } catch (Exception e) {
            e.printStackTrace();
            bounceTexture = 0;
        }
    }

    @Override
    public int getTexture() {
        return bounceTexture;
    }

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
