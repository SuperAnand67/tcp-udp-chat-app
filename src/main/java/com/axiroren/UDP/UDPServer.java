package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPServer {

    static final int PORT = 8088;

    static String HOST = "localhost";

    static volatile InetAddress clientAddr;
    static volatile int clientPort = -1;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            int count = 1;

            Scanner sc = new Scanner(System.in);

            Thread reciever = new Thread(() -> {
                try {
                    while (true) {
                        byte[] recieve = new byte[1024];

                        DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                        socket.receive(recievePacket);

                        String recievedMsg = new String(
                            recievePacket.getData(),
                            0,
                            recievePacket.getLength(),
                            java.nio.charset.StandardCharsets.UTF_8
                        );

                        int start = recievedMsg.indexOf("SEQ ") + 4;
                        int end = recievedMsg.indexOf("]");

                        int seq = Integer.parseInt(
                            recievedMsg.substring(start, end).trim()
                        );

                        String msg = recievedMsg.substring(end + 1).trim();

                        if (msg.equals("exit"))
                            break;

                        System.out.println("\nClient " + "[SEQ " + seq + "] : " + msg);  

                        clientAddr = recievePacket.getAddress();
                        clientPort = recievePacket.getPort();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            );

            reciever.setDaemon(true);
            reciever.start();

            while (true) {

                if (clientAddr == null || clientPort == -1) {
                    System.out.println("Waiting for a client...");
                    continue;
                }

                System.out.print("Enter msg : ");
                String sendMsg = sc.nextLine();

                sendMsg = "[SEQ " + (count++) + "] " + sendMsg;

                byte[] sendBuffer = sendMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    clientAddr,
                    clientPort
                );
                socket.send(sendPacket);

                int end = sendMsg.indexOf("]");

                if (sendMsg.substring(end + 1).trim().equalsIgnoreCase("exit")) {
                    break;
                }

            }

            sc.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
