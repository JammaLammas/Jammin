package com.jammallamas.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jammallamas.Entity;
import com.jammallamas.Main;
import com.jammallamas.Renderable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class NetworkManager {
    private static final byte[] buf = new byte[4096];
    public static boolean connected = false;
    public static boolean isHosting = false;
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Renderable.class, new JsonInheritanceDeserializer<Renderable>())
            .registerTypeAdapter(Entity.class, new JsonInheritanceDeserializer<Entity>())
            .create();
    private static InetAddress pAddress;
    private static int pPort;
    private static DatagramSocket s;

    public static void openServer(int port) {
        isHosting = true;
        try {
            s = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            s.receive(packet);
            pAddress = packet.getAddress();
            pPort = packet.getPort();
            String received = new String(packet.getData(), 0, packet.getLength());
            if (received.equals("hi")) {
                //yep, he's the good guy
                DatagramPacket sent = new DatagramPacket(buf, buf.length, pAddress, pPort);
                s.send(sent);
                connected = true;
                while (true) {
                    //infinite loop !
                    packet = new DatagramPacket(buf, buf.length);
                    s.receive(packet);
                    if (!packet.getAddress().equals(pAddress) || packet.getPort() != pPort) {
                        System.out.println("wrong address ? got " + packet.getAddress() + ":" + packet.getPort() + " expected " + pAddress + ":" + pPort);
                        packet = new DatagramPacket("end".getBytes(), "end".getBytes().length, packet.getAddress(), packet.getPort());
                        s.send(packet);
                        continue; //nope !
                    }
                    received = new String(packet.getData(), 0, packet.getLength());
                    try {
                        int id = Integer.parseInt(String.valueOf(received.charAt(0)));
                        if (id == 2) {
                            Main.bits = Integer.parseInt(received.substring(1));
                        }
                    } catch (NumberFormatException e) {
                        if (received.equals("end")) {
                            break;
                        } else {
                            System.out.println("Bad id ! ignoring !");
                        }
                    }
                }
            }
            connected = false;
            s.close();
        } catch (SocketException ignored) {
            //probably because it was ended elsewhere
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ONLY USE IN CLIENT SIDE !
     *
     * @param bits bit array for keys
     */
    public static void sendKeys(int bits) {
        String st = "2" + bits;
        DatagramPacket sent = new DatagramPacket(st.getBytes(), st.getBytes().length, pAddress, pPort);
        try {
            s.send(sent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ONLY USE IN SERVER SIDE !
     *
     * @param gd game data
     */
    public static void sendGameData(GameData gd) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(4096);
        ba.write('1');
        try {
            GZIPOutputStream gos = new GZIPOutputStream(ba);
            gos.write(gson.toJson(gd).getBytes(StandardCharsets.UTF_8));
            gos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatagramPacket sent = new DatagramPacket(ba.toByteArray(), ba.toByteArray().length, pAddress, pPort);
        try {
            //System.out.println("sending to " + pAddress + ":"+pPort);
            s.send(sent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        if (connected) {
            DatagramPacket packet = new DatagramPacket("end".getBytes(), "end".getBytes().length, pAddress, pPort);
            try {
                s.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            connected = false;
            s.close();
        }
    }

    public static void connectToServer(InetAddress address, int port) {
        try {
            pAddress = address;
            pPort = port;
            s = new DatagramSocket();
            System.arraycopy("hi".getBytes(), 0, buf, 0, "hi".getBytes().length);
            DatagramPacket packet = new DatagramPacket("hi".getBytes(), "hi".getBytes().length, address, port);
            s.send(packet);
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);
            String received = new String(recv.getData(), 0, recv.getLength()).trim();
            if (received.equals("hi")) {
                //yep, he's the good guy
                connected = true;
                while (true) {
                    //infinite loop !
                    packet = new DatagramPacket(buf, buf.length);
                    s.receive(packet);
                    if (!packet.getAddress().equals(pAddress) || packet.getPort() != pPort) {
                        System.out.println("wrong address ? got " + packet.getAddress() + ":" + packet.getPort() + " expected " + pAddress + ":" + pPort);
                        packet = new DatagramPacket("end".getBytes(), "end".getBytes().length, packet.getAddress(), packet.getPort());
                        s.send(packet);
                        continue; //nope !
                    }
                    received = new String(packet.getData(), 0, packet.getLength());
                    try {
                        int id = Integer.parseInt(String.valueOf(received.charAt(0)));
                        if (id == 1) {
                            GameData gd = gson.fromJson(new InputStreamReader(new GZIPInputStream(
                                    new ByteArrayInputStream(packet.getData(), 1, packet.getLength() - 1))
                                    , StandardCharsets.UTF_8), GameData.class);
                            Main.triggerUpdate(gd);
                        }
                    } catch (NumberFormatException e) {
                        if (received.equals("end")) {
                            break; //he has ended the connection
                        } else {
                            System.out.println("Bad id ! ignoring !");
                        }
                    }
                }
            }
            //my connection got closed... let's reset !
            Main.currentLevel = 0;
            Main.queueReset();
            connected = false;
            s.close();
        } catch (SocketException ignored) {
            //probably because it was ended elsewhere
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
