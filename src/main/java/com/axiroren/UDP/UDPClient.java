package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

    static final int PORT = 8088;

    static final String HOST = "localhost";

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            
            Scanner in = new Scanner(System.in);

            InetAddress addr = InetAddress.getByName(HOST);
            System.out.println("Connected to UDP Server at " + HOST + ":" + PORT);

            while (true) {

                System.out.println("Enter msg : ");
                String msg = in.nextLine();

                byte[] sendBuffer = msg.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    addr,
                    PORT
                );

                socket.send(sendPacket);

                if (msg.equalsIgnoreCase("exit"))
                    break;

                byte[] recieve = new byte[1024];

                DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                socket.receive(recievePacket);

                String recieveMsg = new String(
                    recievePacket.getData(),
                    0,
                    recievePacket.getLength()
                );

                if (recieveMsg.equals("exit"))
                    break;

                System.out.println("Server : " + recieveMsg);
            }

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
