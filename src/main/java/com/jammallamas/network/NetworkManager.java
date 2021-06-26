package com.jammallamas.network;

import com.jammallamas.Main;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class NetworkManager {
    public static boolean connected = false;
    public static boolean isHosting = false;
    private static byte[] buf = new byte[4096];
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
                        continue; //nope !
                    }
                    received = new String(packet.getData(), 0, packet.getLength());
                    try {
                        int id = Integer.parseInt(String.valueOf(received.charAt(0)));
                        if (id == 2) {
                            int bits = Integer.parseInt(received.substring(1));
                            Main.handleKeys(bits);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Bad id ! ignoring !");
                    }
                }
            }
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
            new ObjectOutputStream(ba).writeObject(gd);
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
                        continue; //nope !
                    }
                    received = new String(packet.getData(), 0, packet.getLength());
                    try {
                        int id = Integer.parseInt(String.valueOf(received.charAt(0)));
                        if (id == 1) {
                            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 1, packet.getLength() - 1));
                            try {
                                GameData gd = ((GameData) ois.readObject());
                                Main.triggerUpdate(gd);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Bad id ! ignoring !");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
