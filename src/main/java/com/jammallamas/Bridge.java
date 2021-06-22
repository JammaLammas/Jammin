package com.jammallamas;

public class Bridge extends Platform implements ButtonLinkable {
    public Bridge() {
        visible = false;
        collidable = false;
    }

    @Override
    public void onButton() {
        visible = !visible;
        collidable = !collidable;
    }
    //TODO cool animation or something
}
