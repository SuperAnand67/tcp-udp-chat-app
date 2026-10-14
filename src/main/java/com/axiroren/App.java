package com.axiroren;

import java.io.IOException;
import java.util.Scanner;

import com.axiroren.TCP.*;
import com.axiroren.UDP.*;

public class App {
    public static void main( String[] args ) throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.println("Simple TCP/UDP Chat App");

        System.out.println("1. Start TCP Server");
        System.out.println("2. Start TCP Client");
        System.out.println("3. Start UDP Server");
        System.out.println("4. Start UDP Client");
        System.out.println("5. Exit");
        System.out.print("Choose an Option : ");
        
        int choice;
        try {
            choice = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number from 1 to 5.");
            return;
        }

        in.nextLine();

        switch (choice) {
            case 1 -> TCPServer.main(new String[]{});
            case 2 -> {
                System.out.print("Enter server IP: ");
                String ip = in.nextLine();
                TCPClient.main(new String[]{ip});
            }
            case 3 -> UDPServer.main(new String[]{});
            case 4 -> {
                System.out.print("Enter server IP: ");
                String ip = in.nextLine();
                UDPClient.main(new String[]{ip});
            }
            case 5 -> System.out.println("Exiting...");
            default -> System.out.println("Invalid choice!");
        }
    }
}
