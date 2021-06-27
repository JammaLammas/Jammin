package com.jammallamas;

import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.opengl.GL11.*;

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

    public static void resolveCollision(Entity R1, Renderable R2) {
        if (R1.getLastY() + R1.getHeight() > R2.getLastY() && R2.getLastY() + R2.getHeight() > R1.getLastY()) { // if R1 horizontally aligned with R2
            if (R1.getX() + R1.getWidth() / 2 < R2.getX() + R1.getWidth() / 2) {
                // pushback = the right side of the player - the left side of the wall
                // System.out.println("pushing left " + (R1.getWidth() - Math.abs(R1.getX() - R2.getX())));
                R1.setX(R1.getX() - (R1.getWidth() - Math.abs(R1.getX() - R2.getX())));
            } else {
                // pushback = the right side of the wall - the left side of the player
                // System.out.println("pushing right " + (R2.getWidth() - Math.abs(R1.getX() - R2.getX())));
                R1.setX(R1.getX() + (R2.getWidth() - Math.abs(R1.getX() - R2.getX())));
            }
            R1.setxVelocity(0);
        } else if (R1.getLastX() + R1.getWidth() > R2.getX() && R2.getX() + R2.getWidth() > R1.getX()) { // if R2 vertically aligned with R2
            if (R1.getY() + R1.getHeight() / 2 > R2.getY() + R1.getHeight() / 2) {
                // pushback = the bottom side of the player - the top side of the wall
                // System.out.println("pushing down " + (R2.getHeight() - Math.abs((R2.getY() - R1.getY()))));
                R1.setY(R1.getY() + R2.getHeight() - Math.abs((R2.getY() - R1.getY())));
                R1.setOnGround(true);
            } else {
                // pushback = the bottom side of the wall - the top side of the player
                // System.out.println("pushing up " + (R1.getHeight() - Math.abs((R2.getY() - R1.getY()))));
                R1.setY(R1.getY() - R1.getHeight() - Math.abs((R2.getY() - R1.getY())));
            }
            R1.setyVelocity(0);
        }
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

    public static int loadTexture(String fileName) throws Exception {
        int width;
        int height;
        ByteBuffer buf;
        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = STBImage.stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                throw new Exception("Image file [" + fileName + "] not loaded: " + STBImage.stbi_failure_reason());
            }
            width = w.get();
            height = h.get();
        }

        // Create a new OpenGL texture
        int textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        // Generate Mip Map
        GL30.glGenerateMipmap(GL_TEXTURE_2D);

        STBImage.stbi_image_free(buf);

        return textureId;
    }

    public static boolean hasOGLContext() {
        return glfwGetCurrentContext() != 0;
    }
}
