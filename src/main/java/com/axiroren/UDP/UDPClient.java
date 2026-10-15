package com.axiroren.UDP;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.atomic.*;

public class UDPClient {

    static final int PORT = 8088;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("\n========== Statistics ==========");
        System.out.println("Messages sent : " + messagesSent);
        System.out.println("Messages received : " + messagesReceived.get());
        System.out.println("Bytes sent : " + bytesSent);
        System.out.println("Bytes received : " + bytesReceived.get());
    }

    public static void startClient(String HOST) {

        long messagesSent = 0;
        AtomicLong messagesReceived = new AtomicLong();
        long bytesSent = 0;
        AtomicLong bytesReceived = new AtomicLong();

        Scanner in = new Scanner(System.in);

        //String HOST = HOST;

        Map<Integer, Long> pending = new ConcurrentHashMap<>();

        final long ACK_TIMEOUT_NANOS = 2_000_000_000L;

        try (DatagramSocket socket = new DatagramSocket()) {

        System.out.println("Type \"exit\" to Exit !");
        System.out.println("Type /stats for statistics !");
        System.out.println("Start Chatting !");

            int count = 1;

            InetAddress addr = InetAddress.getByName(HOST);

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

                        if (!recieveMsg.startsWith("ACK|")) {
                            messagesReceived.incrementAndGet();
                            bytesReceived.addAndGet(recievePacket.getLength());
                        }

                        if (recieveMsg.startsWith("ACK|")) {
                            int ackSeq = Integer.parseInt(
                                recieveMsg.substring(4).trim()
                            );

                            Long sentAt = pending.remove(ackSeq);

                            if (sentAt != null) {
                                double rttMs =
                                    (System.nanoTime() - sentAt) / 1_000_000.0;

                                System.out.printf(
                                    "\n[ACK] SEQ %d | RTT: %.3f ms%n",
                                    ackSeq,
                                    rttMs
                                );
                            }
                            continue;
                        }

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

            Thread timeoutWatcher = new Thread(() -> {
                while (!socket.isClosed()) {
                    long now = System.nanoTime();

                    for (Map.Entry<Integer, Long> entry : pending.entrySet()) {
                        if (now - entry.getValue() >= ACK_TIMEOUT_NANOS
                                && pending.remove(entry.getKey(), entry.getValue())) {

                            System.out.println(
                                "\n[TIMEOUT] SEQ " + entry.getKey()
                                + " received no ACK. Possible packet loss."
                            );
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "udp-timeout-watcher");

            timeoutWatcher.setDaemon(true);
            timeoutWatcher.start();

            while (true) {

                //System.out.print("Enter msg : ");
                String msg = in.nextLine();

                if (msg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                if (msg.equalsIgnoreCase("exit"))
                    break;

                int seq = count++;
                msg = "[SEQ " + seq + "] " + msg;

                //int end = msg.indexOf("]");

                //String newMsg = msg.substring(end + 1).trim();

                byte[] sendBuffer = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    addr,
                    PORT
                );

                pending.put(seq, System.nanoTime());

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
