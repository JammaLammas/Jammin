package com.jammallamas;

import com.jammallamas.network.GameData;
import com.jammallamas.network.NetworkManager;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public static final ArrayList<Entity> forDeletion = new ArrayList<>();
    public static final byte
            KEY_UP = 0b1,
            KEY_LEFT = 0b10,
            KEY_RIGHT = 0b100,
            KEY_DOWN = 0b1000;
    private static final int DOUBLE_TAP_DELAY = 300;
    private static final double GROUND_FRICTION = 0.25;
    private static final double AIR_FRICTION = 0.00;
    private static final long GRAB_COOLDOWN = 200;
    private static final String[] levels = new String[]{
//		"/testLevel.lvl.gz",
		"/level1.lvl.gz",
		"/level2.lvl.gz",
		"/level3.lvl.gz",
		"/level4.lvl.gz",
		"/level5.lvl.gz",
		"/level6.lvl.gz",
		"/level7.lvl.gz",
    };
    public static int currentLevel = 0;
    public static double cameraX = 0;
    public static double cameraY = 800;
    public static ArrayList<Entity> entities = new ArrayList<>();
    public static ArrayList<Renderable> platforms = new ArrayList<>();
    public static Renderable menu = new Platform();
    public static Player player1;
    public static Player player2;
    public static long grabTimeout = 0;
    public static boolean isGrabbed = false;
    public static boolean isLoading = false;
    public static boolean isPaused = false;
    private static long window;
    private static int windowWidth;
    private static int windowHeight;
    private static boolean resized;
    private static long device;
    private static long context;
    private static long lastPressed = 0;
    private static long lastPressedL = 0;
    private static boolean reset = false;
    private static GameData gd = null;

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

    public static void handleKeys(int bits) {
        if ((bits & KEY_UP) == KEY_UP) {
            Projectile p = new Projectile();
            p.setY(player2.getY() + player2.getHeight());
            p.setX(player2.getX() + player2.getWidth() / 2);
            p.setWidth(2);
            p.setHeight(5);
            p.setyVelocity(10);
            entities.add(p);
        }
        if ((bits & KEY_LEFT) == KEY_LEFT) {
            Projectile p = new Projectile();
            p.setY(player2.getY() + player2.getHeight() / 2);
            p.setX(player2.getX() - 5);
            p.setWidth(5);
            p.setHeight(2);
            p.setxVelocity(-10);
            entities.add(p);
        }
        if ((bits & KEY_RIGHT) == KEY_RIGHT) {
            Projectile p = new Projectile();
            p.setY(player2.getY() + player2.getHeight() / 2);
            p.setX(player2.getX() + player2.getWidth());
            p.setWidth(5);
            p.setHeight(2);
            p.setxVelocity(10);
            entities.add(p);
        }
        if ((bits & KEY_DOWN) == KEY_DOWN) {
            Projectile p = new Projectile();
            p.setY(player2.getY());
            p.setX(player2.getX() + player2.getWidth() / 2);
            p.setWidth(2);
            p.setHeight(5);
            p.setyVelocity(-10);
            entities.add(p);
        }
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
        //300x300
        window = glfwCreateWindow(600, 600, "Hello Jammers!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            // menu key: continue, help, quit

            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {
                isPaused = !isPaused;
            }
            if (!isPaused) {
                // Player 1 movement: WASD

                // D key: walk right if pressed, stop if released
                if (key == GLFW_KEY_D && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {

                    if (player1.isOnGround() && lastPressed + DOUBLE_TAP_DELAY >= System.currentTimeMillis()) {
                        player1.setxVelocity(player1.getSpeed() * 5);
                        player1.setWalking((byte) 0);
                    } else {
                        player1.setWalking((byte) (player1.getWalking() == -1 ? 0 : 1));
                    }
                    lastPressed = System.currentTimeMillis();
                }
                if (key == GLFW_KEY_D && action == GLFW_RELEASE && (!NetworkManager.connected || NetworkManager.isHosting)) {
                    player1.setWalking((byte) 0);
                }

                // A key: walk left if pressed, stop if released
                if (key == GLFW_KEY_A && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {
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

                // Space key: Jump
                if (key == GLFW_KEY_SPACE && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {
                    if (player1.isOnGround()) {
                        player1.setyVelocity(25);
                    }
                }

                // S key: Dash, with a small dive.
                if (key == GLFW_KEY_S && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {
                    if (!player1.isOnGround()) {
                        player1.setyVelocity(player1.getyVelocity() - 2);
                        player1.setxVelocity(player1.getxVelocity() + 8 * player1.getWalking());
                    }
                }

                // Player 2 shooting: Arrow Keys
                int changedArrows = 0;
                if (key == GLFW_KEY_UP && (action == GLFW_PRESS || action == GLFW_REPEAT) && (!NetworkManager.connected || !NetworkManager.isHosting)) {
                    Projectile p = new Projectile();
                    p.setY(player2.getY() + player2.getHeight());
                    p.setX(player2.getX() + player2.getWidth() / 2);
                    p.setWidth(2);
                    p.setHeight(5);
                    p.setyVelocity(10);
                    entities.add(p);
                    changedArrows |= KEY_UP;
                }
                if (key == GLFW_KEY_LEFT && (action == GLFW_PRESS || action == GLFW_REPEAT) && (!NetworkManager.connected || !NetworkManager.isHosting)) {
                    Projectile p = new Projectile();
                    p.setY(player2.getY() + player2.getHeight() / 2);
                    p.setX(player2.getX() - 5);
                    p.setWidth(5);
                    p.setHeight(2);
                    p.setxVelocity(-10);
                    entities.add(p);
                    changedArrows |= KEY_LEFT;
                }
                if (key == GLFW_KEY_RIGHT && (action == GLFW_PRESS || action == GLFW_REPEAT) && (!NetworkManager.connected || !NetworkManager.isHosting)) {
                    Projectile p = new Projectile();
                    p.setY(player2.getY() + player2.getHeight() / 2);
                    p.setX(player2.getX() + player2.getWidth());
                    p.setWidth(5);
                    p.setHeight(2);
                    p.setxVelocity(10);
                    entities.add(p);
                    changedArrows |= KEY_RIGHT;
                }
                if (key == GLFW_KEY_DOWN && (action == GLFW_PRESS || action == GLFW_REPEAT) && (!NetworkManager.connected || !NetworkManager.isHosting)) {
                    Projectile p = new Projectile();
                    p.setY(player2.getY());
                    p.setX(player2.getX() + player2.getWidth() / 2);
                    p.setWidth(2);
                    p.setHeight(5);
                    p.setyVelocity(-10);
                    entities.add(p);
                    changedArrows |= KEY_DOWN;
                }
                if (changedArrows != 0 && NetworkManager.connected && !NetworkManager.isHosting) {
                    NetworkManager.sendKeys(changedArrows);
                }

                // Player 1 throw
                if (key == GLFW_KEY_Y && action == GLFW_PRESS && (!NetworkManager.connected || NetworkManager.isHosting)) {
                    if (isGrabbed) {
                        if (getMulRotation() == 0) {
                            // Put down in front
                            player2.setX(player2.getX() + player2.getWidth());
                        } else {
                            // Yeet
                            player2.setxVelocity(20 * getMulRotation());
                        }
                        grabTimeout = System.currentTimeMillis() + GRAB_COOLDOWN;
                        isGrabbed = false;
                    }
                }
                if (key == GLFW_KEY_C && action == GLFW_PRESS && !NetworkManager.connected) {
                    String address = JOptionPane.showInputDialog(null, "Insert host's ip separated by a colon (ex : 127.0.0.1:25565)", "Connection", JOptionPane.INFORMATION_MESSAGE);
                    new Thread(() -> {
                        try {
                            NetworkManager.connectToServer(InetAddress.getByName(address.split(":")[0]), Integer.parseInt(address.split(":")[1]));
                        } catch (UnknownHostException e) {
                            JOptionPane.showMessageDialog(null, "Error ! Bad ip address");
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Error ! Bad port !");
                        }
                    }).start();
                }
                if (key == GLFW_KEY_H && action == GLFW_PRESS && !NetworkManager.connected) {
                    String address = JOptionPane.showInputDialog(null, "Insert the port you want to host at", "Hosting", JOptionPane.INFORMATION_MESSAGE);
                    new Thread(() -> {
                        try {
                            NetworkManager.openServer(Integer.parseInt(address));
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Error ! Bad port !");
                        }
                    }).start();
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

    /**
     * Give the magic number for the rotation of player1
     *
     * @return 1 if player is facing right, -1 if player is facing left
     */
    private static int getMulRotation() {
        return (int) Math.signum(player1.getX() - player1.getLastX());
    }

    private static void loadLevel(String name) {
        platforms.clear();
        String data = Utils.loadGzResource(name);
        String[] plats = data.split("\n");
        Button lastButton = null;
        for (String pl : plats) {
            if (pl.trim().length() == 0 || pl.startsWith("//")) {
                continue;
            }
            String[] coords = pl.split(":");
            //p:x:y:h:w
            if (coords.length == 5 && coords[0].equals("p")) {
                Platform p = new Platform();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    platforms.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
                //b:x:y:h:w
            } else if (coords.length == 5 && coords[0].equals("b")) {
                Button p = new Button();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    platforms.add(p);
                    lastButton = p;
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("br")) {
                Bridge p = new Bridge();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    if (lastButton == null) {
                        System.err.println("Bridge was tried to be created but no button was found ! " +
                                "Skipping for line : " + pl);
                        continue;
                    }
                    platforms.add(p);
                    lastButton.setAction(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("bro")) {
                Bridge p = new Bridge(true, true);
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    if (lastButton == null) {
                        System.err.println("Bridge was tried to be created but no button was found ! " +
                                "Skipping for line : " + pl);
                        continue;
                    }
                    platforms.add(p);
                    lastButton.setAction(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("d")) {
                DeathPlane p = new DeathPlane();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    platforms.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("end")) {
                FinalDoor p = new FinalDoor();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    platforms.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("bite")) {
                Biter p = new Biter();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    entities.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("dropBite")) {
                Biter p = new Biter();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
                    p.setDropsEdges(true);
                    entities.add(p);
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for line " + pl + "skipping");
                }
            } else if (coords.length == 5 && coords[0].equals("bounce")) {
                BouncyPlatform p = new BouncyPlatform();
                try {
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2]));
                    p.setX(Double.parseDouble(coords[1]));
                    p.setY(Double.parseDouble(coords[2])); // duplicated because lastx and lasty
                    p.setHeight(Double.parseDouble(coords[3]));
                    p.setWidth(Double.parseDouble(coords[4]));
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

    private static void checkState() {
        if (gd != null) {
            currentLevel = gd.currentLevel;
            cameraX = gd.cameraX;
            cameraY = gd.cameraY;
            entities = gd.entities;
            platforms = gd.platforms;
            menu = gd.menu;
            player1 = gd.player1;
            player2 = gd.player2;
            grabTimeout = gd.grabTimeout;
            isGrabbed = gd.isGrabbed;
            isLoading = gd.isLoading;
            isPaused = gd.isPaused;

            gd = null;
            return;
        }
        if (!isPaused) {
            runGameLogic();
        }
        //update game state
        if (NetworkManager.connected && NetworkManager.isHosting) {
            NetworkManager.sendGameData(new GameData(currentLevel, cameraX, cameraY, entities, platforms, menu, player1, player2, grabTimeout, isGrabbed, isLoading, isPaused));
        }
    }

    private static void runGameLogic() {
        // Entity movement
        for (Entity e : entities) {
            if (!(e instanceof Projectile)) {
                // Gravity
                if (isGrabbed && e == player2) {
                    continue;
                }
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
                if (isGrabbed) {
                    player2.setX(player1.getX());
                    player2.setY(player1.getY() + player1.getHeight() + 3); // 3 for spacing
                }
            }
            if (e instanceof Biter) {
                Biter b = (Biter) e;
                b.setChase(player1);
                if (!b.isChasing()) {
                    b.setChase(player2);
                    b.setAccel(player2);
                } else {
                    b.setAccel(player1);
                }
                b.setxVelocity(b.getAccel());
            }
        }
        // Entity-Platform collisions
        for (Entity e : entities) {
            if (!e.collidable) {
                continue;
            }
            e.setOnGround(false);  // Default case, if onGround then will be set so below
            for (Renderable p : platforms) {
                if (!p.collidable) {
                    continue;
                }
                if (Utils.intersects(e, p)) {
                    if (!(e instanceof Player) && p instanceof FinalDoor) {
                        continue;//no collide
                    }
                    if (e instanceof ActionOnTouch) {
                        if (((ActionOnTouch) e).onHit(p)) {
                            forDeletion.add(e);
                        }
                        if (!(e instanceof Projectile)) {
                            Utils.resolveCollision(e, p);
                        }
                    }
                    if (p instanceof ActionOnTouch) {
                        ((ActionOnTouch) p).onHit(e);
                        if (!(p instanceof BouncyPlatform)) { //bouncy platform
                            Utils.resolveCollision(e, p);
                        }
                    }
                    if (!(e instanceof ActionOnTouch) && !(p instanceof ActionOnTouch)) { //TODO will maybe cause errors in the future, be careful
                        Utils.resolveCollision(e, p);
                    }
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
            if (!e.collidable) {
                continue;
            }
            for (int j = 0, size = entities.size(); j < size; j++) {
                Entity other = entities.get(j);
                if (!other.collidable) {
                    continue;
                }
                if (i == j) {
                    continue; // no self-collision
                }
                if (Utils.intersects(e, other)) {
                    if (e instanceof ActionOnTouch) {
                        if (((ActionOnTouch) e).onHit(other)) {
                            forDeletion.add(e);
                        }
                    } else if (other instanceof ActionOnTouch) {
                        if (((ActionOnTouch) other).onHit(e)) {
                            forDeletion.add(other);
                        }
                    } else {//projectile on projectile ?
                        if (e instanceof Player && other instanceof Player) {
                            //player collision !
                            if (grabTimeout > System.currentTimeMillis()) {
                                continue; //nope too soon !
                            }
                            // let's do the grab thingy
                            isGrabbed = true;
                            //teleport second on top
                            player2.setX(player1.getX());
                            player2.setY(player1.getY() + player1.getHeight() + 3); // 3 for spacing

                        } else
                            Utils.resolveCollision(e, other);
                    }
                }
            }
        }
        for (Entity i : forDeletion) {
            entities.remove(i);
        }
        forDeletion.clear();
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
        // Camera following p1
        cameraX = player1.getX() - 300;
        cameraY = player1.getY() + 800;
        if (reset) {
            resetLevel();
        }
    }

    public static void queueReset() {
        reset = true;
    }

    private static void loop() {
        GL.createCapabilities();
        glEnable(GL_TEXTURE_2D);

        // Set the clear color
        glClearColor(0.0f, 1.0f, 1.0f, 0.0f);

        // Player 1 init
        player1 = new Player();
        player1.setX(150);
        player1.setY(0);
        player1.setWidth(30);
        player1.setHeight(60);
        entities.add(player1);

        // Player 2 init
        player2 = new Player();
        player2.setX(190);
        player2.setY(0);
        player2.setWidth(30);
        player2.setHeight(30);
        entities.add(player2);

        // menu
        menu.setX(0);
        menu.setY(-1200);
        menu.setHeight(1200);
        menu.setWidth(1200);


        loadLevel(levels[currentLevel]);


        glTranslated(-1, 1, 0);

        int texture;
        try {
            texture = loadTexture("Background.jpg");
        } catch (Exception e) {
            e.printStackTrace();
            texture = 0;
        }

        int buttonTexture;
        try {
            buttonTexture = loadTexture("button.png");
        } catch (Exception e) {
            e.printStackTrace();
            buttonTexture = 0;
        }

        int doorTexture;
        try {
            doorTexture = loadTexture("door.png");
        } catch (Exception e) {
            e.printStackTrace();
            doorTexture = 0;
        }

        int menuTexture;
        try {
            menuTexture = loadTexture("menu.png");
        } catch (Exception e) {
            e.printStackTrace();
            menuTexture = 0;
        }

        int platformTexture;
        try {
            platformTexture = loadTexture("platform.png");
        } catch (Exception e) {
            e.printStackTrace();
            platformTexture = 0;
        }

        int springTexture;
        try {
            springTexture = loadTexture("spring.png");
        } catch (Exception e) {
            e.printStackTrace();
            springTexture = 0;
        }

        int p1Texture;
        try {
            p1Texture = loadTexture("p1.png");
        } catch (Exception e) {
            e.printStackTrace();
            p1Texture = 0;
        }

        int p2Texture;
        try {
            p2Texture = loadTexture("p2.png");
        } catch (Exception e) {
            e.printStackTrace();
            p2Texture = 0;
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
            glScaled(1 / 600d, 1 / 600d, 1);

            //background
            glPushMatrix();
            glTranslated(0, -1200, 0);
            //glTranslated(Main.cameraX,Main.cameraY,0);
            glBindTexture(GL_TEXTURE_2D, texture);
            glBegin(GL_QUADS);
            glColor4f(1, 1, 1, 1);
            glTexCoord2f(0, 1);
            glVertex2d(0, 0);
            glTexCoord2f(1, 1);
            glVertex2d(0, 1200);
            glTexCoord2f(0, 0);
            glVertex2d(1200, 1200);
            glTexCoord2f(1, 0);
            glVertex2d(1200, 0);
            glEnd();
            glPopMatrix();
            //
            if (isPaused) {
                glColor4f(1, 1, 1, 1);
                glBindTexture(GL_TEXTURE_2D, menuTexture);
                //render menu !
                glPushMatrix();
                glTranslated(menu.getX() + menu.getWidth(), menu.getY(), 0);
                glScaled(-1, 1, 1);
                glBegin(GL_QUADS);
                glTexCoord2f(1, 1);
                glVertex2d(0, 0);
                glTexCoord2f(1, 0);
                glVertex2d(0, menu.getHeight());
                glTexCoord2f(0, 0);
                glVertex2d(menu.getWidth(), menu.getHeight());
                glTexCoord2f(0, 1);
                glVertex2d(menu.getWidth(), 0);
                glEnd();
                glPopMatrix();
            } else {
                for (Renderable pl : platforms) {
                    if (pl instanceof Button) {
                        glColor4f(1, 1, 1, 1);
                        glBindTexture(GL_TEXTURE_2D, buttonTexture);
                    } else if (pl instanceof FinalDoor) {
                        glColor4f(1, 1, 1, 1);
                        glBindTexture(GL_TEXTURE_2D, doorTexture);
                    } else if (pl instanceof BouncyPlatform) {
                        glColor4f(1, 1, 1, 1);
                        glBindTexture(GL_TEXTURE_2D, springTexture);
                    } else if (pl instanceof Platform) {
                        //place anything based on platforms before this if
                        glColor4f(1, 1, 1, 1);
                        glBindTexture(GL_TEXTURE_2D, platformTexture);
                    } else {
                        glColor4f(0, 0, 0, 0);
                        glBindTexture(GL_TEXTURE_2D, 0);
                    }
                    if (pl.visible) {
                        pl.render();
                    }
                }
                glBindTexture(GL_TEXTURE_2D, 0);
                glColor4f(1, 1, 1, 1);
                for (Entity e : entities) {
                    if (e.visible) {
                        if (e == player2) {
                            glBindTexture(GL_TEXTURE_2D, p2Texture);
                        } else if (e == player1) {
                            glBindTexture(GL_TEXTURE_2D, p1Texture);
                        } else {
                            glBindTexture(GL_TEXTURE_2D, 0);
                        }
                        e.render();
                    }
                }
            }
            glPopMatrix();
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            checkState();
        }
    }

    public static Renderable getPlatformAt(double x, double y) {
        Renderable r = new Renderable() {
            @Override
            public void render() {
                //noop
            }
        };
        r.setX(x);
        r.setY(y);
        r.setHeight(1);
        r.setWidth(1);
        for (Renderable p : platforms) {
            if (p.collidable) {
                if (Utils.intersects(p, r)) {
                    return p;
                }
            }
        }
        return null;
    }

    private static void resetLevel() {
        System.out.println("clearing out !");
        reset = false;
        entities.clear();
        platforms.clear();

        // Player 1 init
        player1 = new Player();
        player1.setX(150);
        player1.setY(0);
        player1.setWidth(30);
        player1.setHeight(60);
        entities.add(player1);

        // Player 2 init
        player2 = new Player();
        player2.setX(190);
        player2.setY(0);
        player2.setWidth(30);
        player2.setHeight(30);
        entities.add(player2);

        cameraX = 0;
        cameraY = 800;

        loadLevel(levels[currentLevel]);
        isLoading = false;
    }

    public static void triggerUpdate(GameData gameData) {
        gd = gameData;
    }

}
