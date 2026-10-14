package com.axiroren.TCP;

import java.net.*;
import java.io.*;

public class TCPServer {
    
    static final int PORT = 8088;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Starting Server....... ");
            Socket socket = serverSocket.accept();

            System.out.println("Client Connected : " + socket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            Thread reciever = new Thread(() -> {
                String msg;
                System.out.print("Enter msg : ");
                try {
                    while ((msg = in.readLine()) != null) {
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
                String msg = userIn.readLine();

                if (msg.equals("exit")) 
                    break;

                out.println(msg);
                
            }

            socket.close();
            serverSocket.close();
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }

}
