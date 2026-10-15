package com.axiroren.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.*;

/**
 * Client
 */
public class TCPClient {

    static final int PORT = 8088;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("========== Statistics ==========");
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

        //String HOST = args[0];
        System.out.println("Connecting to Server....");
        try(Socket socket = new Socket(HOST,PORT)) {

            System.out.println("Connected !");
            System.out.println("Type \"exit\" to Exit !");
            System.out.println("Type /stats for statistics !");
            System.out.println("Start Chatting !");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Thread reciever = new Thread( () -> {
                    String msg;
                    try {
                        while ((msg = input.readLine()) != null) {
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

                if (msg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                if(msg.equals("exit"))
                    break;

                output.println(msg);
                messagesSent++;
                bytesSent += msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}