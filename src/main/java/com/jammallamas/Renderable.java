package com.jammallamas;

import com.google.gson.annotations.SerializedName;

import static org.lwjgl.opengl.GL11.*;

public class Renderable {
    public boolean visible = true;
    public boolean collidable = true;
    private double last_x;
    private double last_y;
    private double x;
    private double y;
    private double height;
    private double width;

    @SerializedName("type")
    private String typeName;

    public Renderable() {
        typeName = getClass().getName();
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.last_y = this.y;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.last_x = this.x;
        this.x = x;
    }

    public double getLastY() {
        return last_y;
    }

    public double getLastX() {
        return last_x;
    }

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

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
