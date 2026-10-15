package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

        Map<Integer, Boolean> diagnosticProbes = new ConcurrentHashMap<>();

        List<Double> diagnoRtts = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger diagnoTimeouts = new AtomicInteger();

        final long ACK_TIMEOUT_NANOS = 2_000_000_000L;

        try (DatagramSocket socket = new DatagramSocket()) {

        System.out.println("Type \"exit\" to Exit !");
        System.out.println("Available Commands : /stats, /diagnose, /simulateloss [0-100]");
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

                                if (diagnosticProbes.containsKey(ackSeq)) {
                                    diagnoRtts.add(rttMs);
                                    diagnosticProbes.remove(ackSeq);
                                }

                                System.out.printf(
                                    "\n[ACK] SEQ %d | RTT: %.3f ms%n",
                                    ackSeq,
                                    rttMs
                                );
                            }
                            continue;
                        }

                        if (!recieveMsg.matches("\\[SEQ [1-9]\\d*\\] .*")) {
                            System.out.println("[INVALID PACKET] Ignoring malformed message");
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
                        if (now - entry.getValue() >= ACK_TIMEOUT_NANOS && pending.remove(entry.getKey(), entry.getValue())) {

                            if (diagnosticProbes.containsKey(entry.getKey())) {
                                diagnoTimeouts.incrementAndGet();
                                diagnosticProbes.remove(entry.getKey());
                            }

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

                if (msg.equals("/diagnose")) {
                    final int probeCount = 10;

                    diagnoRtts.clear();
                    diagnoTimeouts.set(0);

                    System.out.println("\n========== NETCHAT DIAGNOSE ==========");
                    System.out.println("Sending " + probeCount + " UDP probes...");

                    for (int i = 1; i <= probeCount; i++) {
                        int probSeq = count++;

                        String probMsg = "[SEQ " + probSeq + "] [DIAG] probe " + i;

                        byte[] probBuffer = probMsg.getBytes();

                        diagnosticProbes.put(probSeq, Boolean.TRUE);
                        pending.put(probSeq, System.nanoTime());

                        DatagramPacket probePacket = new DatagramPacket(
                            probBuffer,
                            probBuffer.length,
                            addr,
                            PORT
                        );

                        socket.send(probePacket);

                        messagesSent++;
                        bytesSent += probBuffer.length;

                        try{
                            Thread.sleep(50);
                        } catch(InterruptedException e){
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                        while (!diagnosticProbes.isEmpty()) {
                            Thread.sleep(50);
                        }

                        int ackCount = diagnoRtts.size();
                        int timeoutCount = diagnoTimeouts.get();

                        double ackRate = ackCount * 100.0 / probeCount;

                        System.out.println("\n========== DIAGNOSTIC REPORT ==========");
                        System.out.println("Probes sent  : " + probeCount);
                        System.out.println("ACKs received: " + ackCount);
                        System.out.println("ACK timeouts : " + timeoutCount);
                        System.out.printf("ACK rate     : %.1f%%%n", ackRate);

                        if (ackCount > 0) {
                            double min = Double.MAX_VALUE;
                            double max = 0;
                            double sum = 0;

                            for(double rtt : diagnoRtts){
                                min = Math.min(min, rtt);
                                max = Math.max(max, rtt);

                                sum += rtt;
                            }

                            System.out.printf("Min RTT      : %.3f ms%n", min);
                            System.out.printf("Average RTT  : %.3f ms%n", sum / ackCount);
                            System.out.printf("Max RTT      : %.3f ms%n", max);
                        }
                        else{
                            System.out.println("No acknowledged probes; RTT unavailable.");
                        }
                        System.out.println("=======================================");
                        continue;
                    }

                int seq = count++;
                msg = "[SEQ " + seq + "] " + msg;

                int end = msg.indexOf("]");

                String newMsg = msg.substring(end + 1).trim();

                byte[] sendBuffer = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    addr,
                    PORT
                );

                pending.put(seq, System.nanoTime());

                socket.send(sendPacket);

                if (newMsg.equalsIgnoreCase("exit"))
                    break;

                messagesSent++;
                bytesSent += sendBuffer.length;
            }

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
