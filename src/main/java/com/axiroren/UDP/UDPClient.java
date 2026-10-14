package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

    static final int PORT = 8088;

    //static final String HOST = "localhost";

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        System.out.print("Enter server IP: ");
        String host = in.nextLine();

        try (DatagramSocket socket = new DatagramSocket()) {

            int count = 1;

            InetAddress addr = InetAddress.getByName(host);

            Thread reciever = new Thread(() -> {
                try {
                    while (true) {
                        byte[] recieve = new byte[1024];

                        DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                        socket.receive(recievePacket);

                        String recieveMsg = new String(
                            recievePacket.getData(),
                            0,
                            recievePacket.getLength(),
                            java.nio.charset.StandardCharsets.UTF_8
                        );

                        int start = recieveMsg.indexOf("SEQ ") + 4;
                        int end = recieveMsg.indexOf("]");

                        int seq = Integer.parseInt(
                            recieveMsg.substring(start, end).trim()
                        );

                        String msg = recieveMsg.substring(end + 1).trim();

                        if (msg.equals("exit"))
                            break;

                        System.out.println("\nServer " + "[SEQ " + seq + "] : " + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            );

            reciever.setDaemon(true);
            reciever.start();

            while (true) {

                System.out.print("Enter msg : ");
                String msg = in.nextLine();

                msg = "[SEQ " + (count++) + "] " + msg;

                byte[] sendBuffer = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    addr,
                    PORT
                );

                socket.send(sendPacket);

                int end = msg.indexOf("]");

                if (msg.substring(end + 1).trim().equalsIgnoreCase("exit"))
                    break;
            }

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
