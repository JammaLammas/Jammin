package com.jammallamas;

public class Bridge extends Platform implements ButtonLinkable {
    public Bridge() {
        this(false, false);
    }

    public Bridge(boolean isVisible, boolean isCollidable) {
        super();
        visible = isVisible;
        collidable = isCollidable;
    }


    @Override
    public void onButton() {
        visible = !visible;
        collidable = !collidable;
    }
    //TODO cool animation or something
}
