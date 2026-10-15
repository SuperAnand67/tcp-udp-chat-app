package com.axiroren.TCP;

import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.*;;;

public class TCPServer {
    
    static final int PORT = 8088;

    private static void stats(long messagesSent,long bytesSent, AtomicLong messagesReceived,AtomicLong bytesReceived){
        System.out.println("\n========== Statistics ==========");
        System.out.println("Messages sent : " + messagesSent);
        System.out.println("Messages received : " + messagesReceived.get());
        System.out.println("Bytes sent : " + bytesSent);
        System.out.println("Bytes received : " + bytesReceived.get());
    }

    public static void startServer() {

    long messagesSent = 0;
    long bytesSent = 0;
    AtomicLong messagesReceived = new AtomicLong();
    AtomicLong bytesReceived = new AtomicLong();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Starting Server....... ");
            Socket socket = serverSocket.accept();

            System.out.println("Client Connected : " + socket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            Thread reciever = new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {

                        if (msg.startsWith("__DIAG__:")) {
                            String id = msg.substring("__DIAG__:".length());
                            out.println("__DIAG_REPLY__:" + id);
                            continue;
                        }

                        if (msg.equalsIgnoreCase("exit")) break;

                        messagesReceived.incrementAndGet();
                        bytesReceived.addAndGet(
                            msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                        );
                        System.out.println("\nClient : " + msg);
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
                String msg = userIn.readLine();

                if ( msg == null || msg.equals("exit")) 
                    break;

                if (msg.equals("/stats")) {
                    stats(messagesSent, bytesSent, messagesReceived, bytesReceived);
                    continue;
                }

                out.println(msg);
                messagesSent++;
                bytesSent += msg.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8
                ).length;
            }

            socket.close();
            serverSocket.close();
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }

}
