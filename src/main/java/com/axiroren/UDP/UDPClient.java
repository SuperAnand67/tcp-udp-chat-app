package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.atomic.*;

public class UDPClient {

    static final int PORT = 8088;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("Messages sent : " + messagesSent);
        System.out.println("Messages received : " + messagesReceived.get());
        System.out.println("Bytes sent : " + bytesSent);
        System.out.println("Bytes received : " + bytesReceived.get());
    }

    public static void main(String[] args) {

        long messagesSent = 0;
        AtomicLong messagesReceived = new AtomicLong();
        long bytesSent = 0;
        AtomicLong bytesReceived = new AtomicLong();

        Scanner in = new Scanner(System.in);

        System.out.print("Enter server IP: ");
        String host = in.nextLine();

        try (DatagramSocket socket = new DatagramSocket()) {

        System.out.println("Type \"exit\" to Exit !");
        System.out.println("Type /stats for statistics !");
        System.out.println("Start Chatting !");

            int count = 1;

            InetAddress addr = InetAddress.getByName(host);

            Thread reciever = new Thread(() -> {
                try {
                    while (true) {
                        byte[] recieve = new byte[1024];

                        DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                        socket.receive(recievePacket);
                        messagesReceived.incrementAndGet();
                        bytesReceived.addAndGet(recievePacket.getLength());

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

                        if (msg.equalsIgnoreCase("exit"))
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

                //System.out.print("Enter msg : ");
                String msg = in.nextLine();

                msg = "[SEQ " + (count++) + "] " + msg;

                int end = msg.indexOf("]");

                String newMsg = msg.substring(end + 1).trim();

                if (newMsg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                if (newMsg.equalsIgnoreCase("exit"))
                    break;

                byte[] sendBuffer = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    addr,
                    PORT
                );

                socket.send(sendPacket);
                messagesSent++;
                bytesSent += sendBuffer.length;
            }

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
