package com.axiroren.TCP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client
 */
public class TCPClient {

    static final int PORT = 8088;

    public static void main(String[] args) {
        System.out.println("Connecting to Server....");
        try(Socket socket = new Socket("localhost",PORT)) {

            System.out.println("Connected !");
            System.out.println("Type \"exit\" to Exit !");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                
                String msg = userIn.readLine();
                output.println(msg);

                if(msg.equals("exit"))
                    break;

                String serverMsg = input.readLine();
                
                System.out.println("Server : " + serverMsg);
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}