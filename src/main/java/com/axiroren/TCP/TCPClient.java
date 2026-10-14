package com.axiroren.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client
 */
public class TCPClient {

    static final int PORT = 8088;

    public static void main(String[] args) throws IOException {

        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter server IP: ");
        String host = userIn.readLine();
        System.out.println("Connecting to Server....");
        try(Socket socket = new Socket(host,PORT)) {

            System.out.println("Connected !");
            System.out.println("Type \"exit\" to Exit !");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Thread reciever = new Thread( () -> {
                    String msg;
                    System.out.print("Enter msg : ");
                    try {
                        while ((msg = input.readLine()) != null) {
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
                String msg = userIn.readLine();

                if(msg.equals("exit"))
                    break;

                output.println(msg);
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}