package com.jammallamas;

public abstract class Renderable {
    private double last_x;
    private double last_y;
    private double x;
    private double y;
    private double height;
    private double width;

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

    public abstract void render();

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
