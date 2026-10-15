package com.axiroren;

import java.io.IOException;
import java.util.Scanner;

import com.axiroren.TCP.*;
import com.axiroren.UDP.*;

public class App {

    private static Scanner in = new Scanner(System.in);

    private static void startServer(int choice){

        switch(choice){
            case 1 -> TCPServer.startServer();
            case 2 -> UDPServer.startServer();

            default -> System.out.println("Invalid choice!");
        }

    }

    private static void startClient(int choice) throws IOException{
        System.out.print("Enter server IP: ");
        String ip = in.nextLine();
        ip = ip.trim();

        switch(choice){
            case 1 -> TCPClient.startClient(ip);
            case 2 -> UDPClient.startClient(ip);

            default -> System.out.println("Invalid choice!");
        }

    }

    public static void main( String[] args ) throws IOException {

        System.out.println("========== NetChat - a TCP/UDP Chat App ==========");
        System.out.println("1. TCP");
        System.out.println("2. UDP");
        System.out.print("Select Mode : ");

        int choice;
        try {
            choice = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please Select Valid Mode !");
            return;
        }

        if (choice != 1 && choice != 2) {
            System.out.println("Please select TCP (1) or UDP (2).");
            return;
        }

        System.out.println("1. Start Server");
        System.out.println("2. Start Client");
        System.out.println("3. Exit");
        System.out.print("Choose an Option : ");
        
        int ch;
        try {
            ch = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please Select Valid Mode !");
            return;
        }

        switch (ch) {
            case 1 -> startServer(choice);
            case 2 -> startClient(choice);
            case 3 -> System.out.println("Exiting...");

            default -> System.out.println("Invalid choice!");
        }
    }
}
