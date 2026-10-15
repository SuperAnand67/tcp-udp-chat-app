package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;
import java.util.concurrent.ThreadLocalRandom;

public class UDPServer {

    static final int PORT = 8088;
    private static final int LOSS_PERCENT = 20;

    static volatile InetAddress clientAddr;
    static volatile int clientPort = -1;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("\n========== Statistics ==========");
        System.out.println("Messages sent : " + messagesSent);
        System.out.println("Messages received : " + messagesReceived.get());
        System.out.println("Bytes sent : " + bytesSent);
        System.out.println("Bytes received : " + bytesReceived.get());
    }

    public static void startServer() {

        long messagesSent = 0;
        AtomicLong messagesReceived = new AtomicLong();
        long bytesSent = 0;
        AtomicLong bytesReceived = new AtomicLong();

        CountDownLatch clientReady = new CountDownLatch(1);

        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            int count = 1;

            Scanner sc = new Scanner(System.in);

            Thread reciever = new Thread(() -> {
                try {
                    while (true) {
                        byte[] recieve = new byte[1024];

                        DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                        socket.receive(recievePacket);

                        messagesReceived.incrementAndGet();
                        bytesReceived.addAndGet(recievePacket.getLength());

                        String recievedMsg = new String(
                                recievePacket.getData(),
                                0,
                                recievePacket.getLength(),
                                java.nio.charset.StandardCharsets.UTF_8);

                        int start = recievedMsg.indexOf("SEQ ") + 4;
                        int end = recievedMsg.indexOf("]");

                        int seq = Integer.parseInt(
                                recievedMsg.substring(start, end).trim());

                        String msg = recievedMsg.substring(end + 1).trim();

                        if (ThreadLocalRandom.current().nextInt(100) < LOSS_PERCENT) {
                            System.out.println(
                                "\n[SIMULATED DROP] Discarded client SEQ " + seq
                            );
                            continue;
                        }

                        if (msg.equalsIgnoreCase("exit"))
                            break;

                        System.out.println("\nClient " + "[SEQ " + seq + "] : " + msg);

                        clientAddr = recievePacket.getAddress();
                        clientPort = recievePacket.getPort();

                        clientReady.countDown();

                        byte[] ack = ("ACK|" + seq).getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                        );

                        DatagramPacket ackPacket = new DatagramPacket(
                            ack,
                            ack.length,
                            clientAddr,
                            clientPort
                        );

                        socket.send(ackPacket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            reciever.setDaemon(true);
            reciever.start();

            while (true) {

                if (clientAddr == null || clientPort == -1) {
                    System.out.println("Waiting for a client...");
                    clientReady.await();
                    continue;
                }

                //System.out.print("Enter msg : ");
                String sendMsg = sc.nextLine();

                if (sendMsg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                if (sendMsg.equalsIgnoreCase("exit"))
                    break;

                sendMsg = "[SEQ " + (count++) + "] " + sendMsg;

                //int end = sendMsg.indexOf("]");

                //String newMsg = sendMsg.substring(end + 1).trim();

                byte[] sendBuffer = sendMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        clientAddr,
                        clientPort);
                socket.send(sendPacket);

                messagesSent++;
                bytesSent += sendBuffer.length;

            }

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
