package com.axiroren.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPServer {

    static final int PORT = 8088;

    static String HOST = "localhost";

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            Scanner sc = new Scanner(System.in);

            while (true) {
                byte[] recieve = new byte[1024];

                DatagramPacket recievePacket = new DatagramPacket(recieve, recieve.length);

                socket.receive(recievePacket);

                String msg = new String(
                    recievePacket.getData(),
                    0,
                    recievePacket.getLength()
                );

                 if (msg.equals("exit"))
                    break;

                System.out.println("Client : " + msg);  

                InetAddress clientAddr = recievePacket.getAddress();
                int cliPort = recievePacket.getPort();

                System.out.println("Enter msg : ");
                String sendMsg = sc.nextLine();

                byte[] sendBuffer = sendMsg.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer, 
                    sendBuffer.length,
                    clientAddr,
                    cliPort
                );
                socket.send(sendPacket);

                if (sendMsg.equalsIgnoreCase("exit")) {
                    break;
                }

            }

            sc.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
