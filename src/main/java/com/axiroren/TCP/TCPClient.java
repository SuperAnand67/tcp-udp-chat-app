package com.axiroren.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Client
 */
public class TCPClient {

    static final int PORT = 8088;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("\n========== Statistics ==========");
        System.out.println("Messages sent : " + messagesSent);
        System.out.println("Messages received : " + messagesReceived.get());
        System.out.println("Bytes sent : " + bytesSent);
        System.out.println("Bytes received : " + bytesReceived.get());
    }

    public static void startClient(String HOST) throws IOException {

        long messagesSent = 0;
        AtomicLong messagesReceived = new AtomicLong();
        long bytesSent = 0;
        AtomicLong bytesReceived = new AtomicLong();

        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

        Map<Integer, Long> diagnosticPending = new ConcurrentHashMap<>();
        List<Double> diagnosticRtts = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger diagnosticId = new AtomicInteger();

        //String HOST = args[0];
        System.out.println("Connecting to Server....");
        try(Socket socket = new Socket(HOST,PORT)) {

            System.out.println("Connected !");
            System.out.println("Type \"exit\" to Exit !");
            System.out.println("Type /stats for statistics !");
            System.out.println("Type /diagnose for diagnostics !");
            System.out.println("Start Chatting !");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Thread reciever = new Thread( () -> {
                    String msg;
                    try {
                        while ((msg = input.readLine()) != null) {

                            if (msg.startsWith("__DIAG_REPLY__:")) {
                                try {
                                    int id = Integer.parseInt(msg.substring("__DIAG_REPLY__:".length()));

                                    Long startTime = diagnosticPending.remove(id);

                                    if (startTime != null) {
                                        double rtt = (System.nanoTime() - startTime) / 1_000_000.0;
                                        diagnosticRtts.add(rtt);
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid diagnostic response.");
                                }
                                continue;
                            }

                            if(msg.equals("exit"))
                                break;

                            messagesReceived.incrementAndGet();
                            bytesReceived.addAndGet(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);

                            System.out.println("\nServer : " + msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            );

            reciever.setDaemon(true);
            reciever.start();

            while (true) {
                //System.out.print("Enter msg : ");
                String msg = userIn.readLine();

                if (msg == null) break;

                if (msg.equals("/diagnose")) {

                    int probeCount = 10;
                    diagnosticRtts.clear();
                    diagnosticPending.clear();

                    System.out.println("\nRunning TCP diagnostics...");

                    for (int i = 0; i < probeCount; i++) {
                        int id = diagnosticId.incrementAndGet();

                        diagnosticPending.put(id, System.nanoTime());
                        output.println("__DIAG__:" + id);

                        messagesSent++;
                        bytesSent += ("__DIAG__:" + id).getBytes().length;

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                    long deadline = System.nanoTime() + 3_000_000_000L;

                    while (!diagnosticPending.isEmpty()
                            && System.nanoTime() < deadline) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                    int received = diagnosticRtts.size();
                    int timedOut = diagnosticPending.size();

                    diagnosticPending.clear();

                    System.out.println("\n===== TCP DIAGNOSTICS =====");
                    System.out.println("Probes sent: " + probeCount);
                    System.out.println("Responses received: " + received);
                    System.out.println("Timed out: " + timedOut);
                    System.out.printf("Response rate: %.1f%%%n",
                            received * 100.0 / probeCount);

                    if (received > 0) {
                        double min = Collections.min(diagnosticRtts);
                        double max = Collections.max(diagnosticRtts);
                        double avg = diagnosticRtts.stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0);

                        System.out.printf("Minimum RTT: %.3f ms%n", min);
                        System.out.printf("Average RTT: %.3f ms%n", avg);
                        System.out.printf("Maximum RTT: %.3f ms%n", max);
                    } else {
                        System.out.println("No diagnostic responses received.");
                    }

                    System.out.println("===========================\n");
                    continue;
                }

                if (msg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                output.println(msg);

                if(msg.equals("exit"))
                    break;  

                messagesSent++;
                bytesSent += msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}