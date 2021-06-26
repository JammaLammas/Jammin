package com.jammallamas.network;

import com.jammallamas.Entity;
import com.jammallamas.Player;
import com.jammallamas.Renderable;

import java.io.Serializable;
import java.util.ArrayList;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1773536857132043952L;
    public int currentLevel;
    public double cameraX;
    public double cameraY;
    public ArrayList<Entity> entities;
    public ArrayList<Renderable> platforms;
    public Renderable menu;
    public Player player1;
    public Player player2;
    public long grabTimeout;
    public boolean isGrabbed;
    public boolean isLoading;
    public boolean isPaused;

    public GameData(int currentLevel, double cameraX, double cameraY, ArrayList<Entity> entities, ArrayList<Renderable> platforms, Renderable menu, Player player1, Player player2, long grabTimeout, boolean isGrabbed, boolean isLoading, boolean isPaused) {
        this.currentLevel = currentLevel;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.entities = entities;
        this.platforms = platforms;
        this.menu = menu;
        this.player1 = player1;
        this.player2 = player2;
        this.grabTimeout = grabTimeout;
        this.isGrabbed = isGrabbed;
        this.isLoading = isLoading;
        this.isPaused = isPaused;
    }
}
