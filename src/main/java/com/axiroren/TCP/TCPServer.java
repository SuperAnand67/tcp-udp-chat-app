package com.axiroren.TCP;

import java.net.*;
import java.io.*;

public class TCPServer {
    
    static final int PORT = 8088;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Starting Server....... ");
            Socket socket = serverSocket.accept();

            System.out.println("Client Connected : " + socket.getLocalAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String cliMsg = in.readLine();

                if(cliMsg.equals("exit"))
                    break;

                System.out.println("You : " + cliMsg);

                String msg = userIn.readLine();

                out.println(msg);

                if (msg.equals("exit")) 
                    break;
            }

            socket.close();
            serverSocket.close();
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }

}
