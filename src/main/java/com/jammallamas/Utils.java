package com.jammallamas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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
}
