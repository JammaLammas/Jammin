package com.jammallamas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static boolean intersects(Renderable r1, Renderable r) {
        double tw = r1.getWidth();
        double th = r1.getHeight();
        double rw = r.getWidth();
        double rh = r.getHeight();
        double tx = r1.getX();
        double ty = r1.getY();
        double rx = r.getX();
        double ry = r.getY();
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      overflow || intersect
        return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
    }


    /**
     * IntersectSide
     */
    public static final int
            TOP = 0b0001,
            BOTTOM = 0b0010,
            LEFT = 0b0100,
            RIGHT = 0b1000;

    public static void resolveCollision(Entity R1, Renderable R2) {
        System.out.println("R1 last Y = " + R1.getLastY());
        System.out.println("R2 last Y = " + R2.getLastY());
        System.out.println("R1 X = " + R1.getX());
        System.out.println("R2 X = " + R2.getX());
        System.out.println(R2.getLastY() + R2.getHeight());
        if (R1.getLastY() > R2.getLastY() + R2.getHeight() && R1.getLastY() + R1.getHeight() > R2.getLastY()) { // if R1 horizontally aligned with R2
            System.out.println("Horizontally Aligned!");
            if (R1.getX() + R1.getWidth() / 2 < R2.getX() + R1.getWidth() / 2) {
                // pushback = the right side of the player - the left side of the wall
                System.out.println("pushing left " + (R1.getWidth() - Math.abs(R1.getX() - R2.getX())));
                R1.setX(R1.getX() - R1.getWidth() - Math.abs(R1.getX() - R2.getX()));
            } else {
                // pushback = the right side of the wall - the left side of the player
                System.out.println("pushing right " + (R2.getWidth() - Math.abs(R1.getX() - R2.getX())));
                R1.setX(R1.getX() + (R2.getWidth() - Math.abs(R1.getX() - R2.getX())));
            }
            R1.setxVelocity(0);
        }
        else if (R1.getLastX() < R2.getLastX() + R2.getWidth() && R1.getLastX() + R1.getWidth() > R2.getLastX()){ // if R2 vertically aligned with R2
            if (R1.getY() + R1.getHeight() / 2 > R2.getY() + R1.getHeight() / 2) {
                // pushback = the bottom side of the player - the top side of the wall
                System.out.println("pushing down " + (R2.getHeight() - Math.abs((R2.getY() - R1.getY()))));
                R1.setY(R1.getY() + R2.getHeight() - Math.abs((R2.getY() - R1.getY())));
                R1.setOnGround(true);
            }
            else {
                // pushback = the bottom side of the wall - the top side of the player
                System.out.println("pushing up " + (R1.getHeight() - Math.abs((R2.getY() - R1.getY()))));
                R1.setY(R1.getY() - R1.getHeight() - Math.abs((R2.getY() - R1.getY())));
            }
            R1.setyVelocity(0);
        }
    }

    /**
     * Gets where it intersects
     * only compute when the first intersects is true
     *
     * @param R1 the "player" renderable (or entity)
     * @param R2 the tile renderable
     * @return 0 for top, 1 for bottom, 2 for left, 3 for right, -1 if not intersecting
     */
    public static int getIntersectsSide(Renderable R1, Renderable R2) {
        final int m = 1; //TODO test values
        int res = 0;
        //x-direction
        if (R1.getX() + m > R2.getX() + R2.getWidth()) {
            res |= LEFT;
        } else if (R1.getX() + R1.getWidth() - m < R2.getX()) {
            res |= RIGHT;
        }

        //y-direction
        if (R1.getY() + m > R2.getY() + R2.getHeight()) {
            res |= TOP;
        } else if (R1.getY() + R1.getHeight() - m < R2.getY()) {
            res |= BOTTOM;
        }
        return res;
    }

    public static String loadResource(String fileName) {
        String result = "";
        try (InputStream in = Utils.class.getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String loadGzResource(String fileName) {
        String result = "";
        try (InputStream in = new GZIPInputStream(Utils.class.getResourceAsStream(fileName));
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
