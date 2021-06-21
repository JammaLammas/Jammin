package com.jammallamas;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private static long window;

    private final static double SPEED = 5;
    public static int cameraX = 0;
    public static int cameraY = 0;
    public static byte walking = 0;
    private static int windowWidth;
    private static int windowHeight;
    private static boolean resized;

    public static void main(String[] args) {
        try {
            init();
            loop();
        } finally {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    public static ArrayList<Entity> entities = new ArrayList<>();
    public static ArrayList<Platform> platforms = new ArrayList<>();
    public static Player player;

    private static final int DOUBLE_TAP_DELAY = 500;
    private static long lastPressed = 0;
    private static long lastPressedL = 0;

    private static void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello Jammers!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            if (key == GLFW_KEY_D && action == GLFW_PRESS) {

                if (lastPressed + DOUBLE_TAP_DELAY >= System.currentTimeMillis()) {
                    player.setxVelocity(SPEED * 10);
                    walking = 0;
                } else {
                    walking = (byte) (walking == -1 ? 0 : 1);
                }
                lastPressed = System.currentTimeMillis();
            }
            if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                walking = 0;
            }
            if (key == GLFW_KEY_A && action == GLFW_PRESS) {
                if (lastPressedL + DOUBLE_TAP_DELAY >= System.currentTimeMillis()) {
                    player.setxVelocity(SPEED * -10);
                    walking = 0;
                } else {
                    walking = (byte) (walking == -1 ? 0 : -1);
                }
                lastPressedL = System.currentTimeMillis();
            }
            if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
                walking = 0;
            }
            if (key == GLFW_KEY_Z && action == GLFW_PRESS) {
                if (!player.isOnGround()) {
                    player.setyVelocity(player.getyVelocity() + 2);
                    player.setxVelocity(8);
                }
            }
            if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                if (player.isOnGround()) {
                    player.setyVelocity(25);
                }
            }
        });
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            windowWidth = width;
            windowHeight = height;
            resized = true;
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private static void loop() {
        GL.createCapabilities();
        glEnable(GL_TEXTURE_2D);

        // Set the clear color
        glClearColor(0.0f, 1.0f, 1.0f, 0.0f);
        Player p = new Player();
        p.setX(20);
        p.setY(-10);
        p.setWidth(30);
        p.setHeight(60);
        entities.add(p);
        player = p;

        Platform plat = new Platform();
        plat.setX(0);
        plat.setY(-600);
        plat.setWidth(600);
        plat.setHeight(20);
        platforms.add(plat);

        loadLevel("/testLevel.lvl.gz");


        glTranslated(-1, 1, 0);

        int texture;
        try {
            texture = loadTexture("Background.jpg");
        } catch (Exception e) {
            e.printStackTrace();
            texture = 0;
        }


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            if (resized) {
                glViewport(0, 0, windowWidth, windowHeight);
                resized = false;
            }

            glPushMatrix();
            glScaled(1 / 300d, 1 / 300d, 1);

            //background
            glPushMatrix();
            glTranslated(0, -600, 0);
            //glTranslated(Main.cameraX,Main.cameraY,0);
            glBindTexture(GL_TEXTURE_2D, texture);
            glBegin(GL_QUADS);
            glColor4f(1, 1, 1, 1);
            glTexCoord2f(0, 1);
            glVertex2d(0, 0);
            glTexCoord2f(1, 1);
            glVertex2d(0, 600);
            glTexCoord2f(0, 0);
            glVertex2d(600, 600);
            glTexCoord2f(1, 0);
            glVertex2d(600, 0);
            glEnd();
            glPopMatrix();
            glBindTexture(GL_TEXTURE_2D, 0);
            //
            for (Platform pl : platforms) {
                pl.render();
            }

            for (Entity e : entities) {
                e.render();
            }
            glPopMatrix();
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            runGameLogic();
        }
    }

    private static void loadLevel(String name) {
        platforms.clear();
        String data = Utils.loadGzResource(name);
        String[] plats = data.split("\n");
        for (String pl : plats) {
            if (pl.startsWith("//")) {
                continue;
            }
            String[] coords = pl.split(":");
            //x:y:h:w
            if (coords.length == 4) {
                Platform p = new Platform();
                try {
                    p.setX(Double.parseDouble(coords[0]));
                    p.setY(Double.parseDouble(coords[1]));
                    p.setHeight(Double.parseDouble(coords[2]));
                    p.setWidth(Double.parseDouble(coords[3]));
                    platforms.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }

            } else {
                //uh uh
                System.err.println("this line " + pl + " is invalid ! skipping");
            }
        }
    }

    private static int loadTexture(String fileName) throws Exception {
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

    private static void runGameLogic() {
        cameraX++;


        player.setyVelocity(player.getyVelocity() - 0.981);
        player.setY(player.getY() + player.getyVelocity());
        player.setX(player.getX() + player.getxVelocity());
        if (walking != 0 && player.isOnGround()) {
            player.setxVelocity(SPEED * walking);
        }
        //player.setX(player.getX() + SPEED * walking);
        boolean onGround = false;
        for (Platform p : platforms) {
            if (Utils.intersects(player, p)) {
                int collide = Utils.getIntersectsSide(player, p);
                if ((collide & Utils.TOP) == Utils.TOP) {
                    //top
                    onGround = true;
                    player.setY(p.getY() + p.getHeight());
                    player.setyVelocity(0);
                }
                if ((collide & Utils.BOTTOM) == Utils.BOTTOM) {
                    //bottom
                    player.setY(p.getY());
                    player.setyVelocity(0);
                }
                if ((collide & Utils.LEFT) == Utils.LEFT) {
                    System.out.println("boopp");
                    player.setX(p.getX() + p.getWidth());
                    player.setxVelocity(0);
                }
                if ((collide & Utils.RIGHT) == Utils.RIGHT) {
                    System.out.println("boop");
                    player.setX(p.getX() - 1);
                    player.setxVelocity(0);
                }
                if (collide == 0) {
                    //what ??? player has collided from nowhere ???? oh no oh no !!!!!
                    System.err.println("collision");
                    player.setX(p.getX());
                    player.setY(p.getY() + p.getHeight());
                    player.setxVelocity(0);
                    player.setyVelocity(0);
                }
            }
        }
        player.setOnGround(onGround);
        final double groundFriction = 0.25;
        final double airFriction = 0.00;
        if (player.getxVelocity() > 0) {
            player.setxVelocity(player.getxVelocity() - player.getxVelocity() * (onGround ? groundFriction : airFriction));
            if (player.getxVelocity() < 0) {
                player.setxVelocity(0);
            }
        } else if (player.getxVelocity() < 0) {
            player.setxVelocity(player.getxVelocity() + -player.getxVelocity() * (onGround ? groundFriction : airFriction));
            if (player.getxVelocity() > 0) {
                player.setxVelocity(0);
            }
        }

    }

}
