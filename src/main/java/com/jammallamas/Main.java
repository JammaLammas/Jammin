package com.jammallamas;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private static final int DOUBLE_TAP_DELAY = 500;
    private static final double GROUND_FRICTION = 0.25;
    private static final double AIR_FRICTION = 0.00;
    public static int cameraX = 0;
    public static int cameraY = 0;
    public static ArrayList<Entity> entities = new ArrayList<>();
    public static ArrayList<Platform> platforms = new ArrayList<>();
    public static Player player1;
    public static Player player2;
    private static long window;
    private static int windowWidth;
    private static int windowHeight;
    private static boolean resized;
    private static long device;
    private static long context;
    private static long lastPressed = 0;
    private static long lastPressedL = 0;

    public static void main(String[] args) {
        try {
            init();
            loop();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ALC10.alcDestroyContext(context);
            ALC10.alcCloseDevice(device);

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
            System.exit(0); //close everything else i forgot
        }
    }

    private static void playMusic(String filename) {
        int sampleRate;
        int channels;
        ShortBuffer rawAudioBuffer;
        try (MemoryStack stack = stackPush()) {
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);
            rawAudioBuffer = stb_vorbis_decode_filename(filename, channelsBuffer, sampleRateBuffer);
            channels = channelsBuffer.get();
            sampleRate = sampleRateBuffer.get();
        }
        //Find the correct OpenAL format
        int format = -1;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        }

        //Request space for the buffer
        int bufferPointer = alGenBuffers();

        double time = sampleRate + 0.0d / rawAudioBuffer.remaining();

        //Send the data to OpenAL
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

        //Free the memory allocated by STB
        LibCStdlib.free(rawAudioBuffer);


        //Request a source
        int sourcePointer = alGenSources();

        //Assign the sound we just loaded to the source
        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);

        //Play the sound
        alSourcePlay(sourcePointer);

        new Thread(() -> {
            try {
                Thread.sleep((long) (time * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //free up
            alDeleteSources(sourcePointer);
            alDeleteBuffers(bufferPointer);
        }).start();
    }

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

        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        device = alcOpenDevice(defaultDeviceName);
        int[] attributes = {0};
        context = ALC10.alcCreateContext(device, attributes);
        ALC10.alcMakeContextCurrent(context);
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello Jammers!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            // Escape key: close
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop

            // Player 1 movement: WASD

            // D key: walk right if pressed, stop if released
            if (key == GLFW_KEY_D && action == GLFW_PRESS) {

                if (player1.isOnGround() && lastPressed + DOUBLE_TAP_DELAY >= System.currentTimeMillis()) {
                    player1.setxVelocity(player1.getSpeed() * 5);
                    player1.setWalking((byte) 0);
                } else {
                    player1.setWalking((byte) (player1.getWalking() == -1 ? 0 : 1));
                }
                lastPressed = System.currentTimeMillis();
            }
            if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                player1.setWalking((byte) 0);
            }

            // A key: walk left if pressed, stop if released
            if (key == GLFW_KEY_A && action == GLFW_PRESS) {
                if (player1.isOnGround() && lastPressedL + DOUBLE_TAP_DELAY >= System.currentTimeMillis()) {
                    player1.setxVelocity(player1.getSpeed() * -5);
                    player1.setWalking((byte) 0);
                } else {
                    player1.setWalking((byte) (player1.getWalking() == -1 ? 0 : -1));
                }
                lastPressedL = System.currentTimeMillis();
            }
            if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
                player1.setWalking((byte) 0);
            }

            // W key: Jump
            if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                if (player1.isOnGround()) {
                    player1.setyVelocity(25);
                }
            }

            // S key: Dash, with a small dive.
            if (key == GLFW_KEY_S && action == GLFW_PRESS) {
                if (!player1.isOnGround()) {
                    player1.setyVelocity(player1.getyVelocity() - 2);
                    player1.setxVelocity(player1.getxVelocity() + 8 * player1.getWalking());
                }
            }

            if (key == GLFW_KEY_UP && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                Projectile p = new Projectile();
                p.setY(player2.getY() + player2.getHeight());
                p.setX(player2.getX() + player2.getWidth() / 2);
                p.setWidth(2);
                p.setHeight(5);
                p.setyVelocity(10);
                entities.add(p);
            }
            if (key == GLFW_KEY_LEFT && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                Projectile p = new Projectile();
                p.setY(player2.getY() + player2.getHeight() / 2);
                p.setX(player2.getX() - 5);
                p.setWidth(5);
                p.setHeight(2);
                p.setxVelocity(-10);
                entities.add(p);
            }
            if (key == GLFW_KEY_RIGHT && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                Projectile p = new Projectile();
                p.setY(player2.getY() + player2.getHeight() / 2);
                p.setX(player2.getX() + player2.getWidth());
                p.setWidth(5);
                p.setHeight(2);
                p.setxVelocity(10);
                entities.add(p);
            }
            if (key == GLFW_KEY_DOWN && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                Projectile p = new Projectile();
                p.setY(player2.getY());
                p.setX(player2.getX() + player2.getWidth() / 2);
                p.setWidth(2);
                p.setHeight(5);
                p.setyVelocity(10);
                entities.add(p);
            }
            // Player 2 shooting: Arrow Keys
            // TODO
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

        // Player 1 init
        player1 = new Player();
        player1.setX(60);
        player1.setY(-10);
        player1.setWidth(30);
        player1.setHeight(60);
        entities.add(player1);

        // Player 2 init
        player2 = new Player();
        player2.setX(100);
        player2.setY(60);
        player2.setWidth(30);
        player2.setHeight(60);
        entities.add(player2);
        //player1 = player2;


        loadLevel("/testLevel.lvl.gz");


        glTranslated(-1, 1, 0);

        int texture;
        try {
            texture = loadTexture("Background.jpg");
        } catch (Exception e) {
            e.printStackTrace();
            texture = 0;
        }

        playMusic("boop.ogg");


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
                    p.setX(Double.parseDouble(coords[0]));
                    p.setY(Double.parseDouble(coords[1])); // duplicated because lastx and lasty
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
        //TODO put a good camera in
        //cameraX = 300;

        // Entity movement
        for (Entity e : entities) {
            if (!(e instanceof Projectile)) {
                // Gravity
                e.setyVelocity(e.getyVelocity() - 0.981);
            }

            // New coordinates after velocity applied
            e.setX(e.getX() + e.getxVelocity());
            e.setY(e.getY() + e.getyVelocity());

            // Add walking velocity to velocity
            if (e instanceof Player) {
                Player p = (Player) e;
                if (p.getWalking() != 0 && p.isOnGround()) {
                    p.setxVelocity(p.getSpeed() * p.getWalking());
                }
            }
        }
        ArrayList<Projectile> forDeletion = new ArrayList<>();
        // Entity-Platform collisions
        for (Entity e : entities) {
            e.setOnGround(false);  // Default case, if onGround then will be set so below
            for (Platform p : platforms) {
                if (Utils.intersects(e, p)) {
                    if (e instanceof Projectile) {
                        if (((Projectile) e).onHit(p)) {
                            forDeletion.add((Projectile) e);
                        }
                        continue;
                    }
                    Utils.resolveCollision(e, p);
                }
            }
        }
        for (Entity i : forDeletion) {
            entities.remove(i);
        }
        forDeletion.clear();
        // Entity-Entity collisions
        for (int i = 0, entitiesSize = entities.size(); i < entitiesSize; i++) {
            Entity e = entities.get(i);
            for (int j = 0, size = entities.size(); j < size; j++) {
                Entity other = entities.get(j);
                if (i == j) {
                    continue; // no self-collision
                }
                if (Utils.intersects(e, other)) {
                    if (e instanceof Projectile) {
                        if (((Projectile) e).onHit(other)) {
                            forDeletion.add((Projectile) e);
                        }
                    } else if (other instanceof Projectile) {
                        if (((Projectile) other).onHit(e)) {
                            forDeletion.add((Projectile) other);
                        }
                    } else//projectile on projectile ?
                        Utils.resolveCollision(e, other);
                }
            }
        }
        for (Entity i : forDeletion) {
            entities.remove(i);
        }

        // Friction
        for (Entity e : entities) {
            if (e instanceof Projectile) {
                continue;
            }
            boolean onGround = e.isOnGround();
            if (e.getxVelocity() > 0) {
                e.setxVelocity(e.getxVelocity() - e.getxVelocity() * (onGround ? GROUND_FRICTION : AIR_FRICTION));
                if (e.getxVelocity() < 0) {
                    e.setxVelocity(0);
                }
            } else if (e.getxVelocity() < 0) {
                e.setxVelocity(e.getxVelocity() + -e.getxVelocity() * (onGround ? GROUND_FRICTION : AIR_FRICTION));
                if (e.getxVelocity() > 0) {
                    e.setxVelocity(0);
                }
            }
        }

    }

}
