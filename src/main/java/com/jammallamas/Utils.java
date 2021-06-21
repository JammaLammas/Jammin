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
     * Gets where it intersects
     * only compute when the first intersects is true
     *
     * @param player the "player" renderable (or entity)
     * @param tile   the tile renderable
     * @return 0 for top, 1 for bottom, 2 for left, 3 for right, -1 if not intersecting
     */
    public static int getIntersectsSide(Renderable player, Renderable tile) {
        double height = Math.abs(player.getHeight());
        double y = Math.abs(player.getY());
        double y1 = Math.abs(tile.getY());
        double x1 = Math.abs(tile.getX());
        double width1 = Math.abs(tile.getWidth());
        double height1 = Math.abs(tile.getHeight());
        double x = Math.abs(player.getX());
        double width = Math.abs(player.getWidth());

        double tile_top = y1 + height1;
        double player_right = x + width;
        double tile_right = x1 + width1;
        double player_top = y + height;

        double b_collision = y1 - player_top; //bottom_t top diff
        double t_collision = y - tile_top; //bottom_p top diff

        double l_collision = player_right - x1;
        double r_collision = tile_right - x;
        if (t_collision < b_collision && t_collision < l_collision && t_collision < r_collision) {
            return 0;
        }
        if (b_collision < t_collision && b_collision < l_collision && b_collision < r_collision) {
            return 1;
        }
        if (l_collision < r_collision && l_collision < t_collision && l_collision < b_collision) {
            return 2;
        }
        if (r_collision < l_collision && r_collision < t_collision && r_collision < b_collision) {
            return 3;
        }
        return -1;
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
