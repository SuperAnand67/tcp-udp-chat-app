package com.axiroren;

import java.util.Scanner;

import com.axiroren.TCP.*;
import com.axiroren.UDP.*;

public class App {
    public static void main( String[] args ) {
        Scanner in = new Scanner(System.in);

        System.out.println("Simple TCP/UDP Chat App");
        System.out.println("1. Start TCP Server");
        System.out.println("2. Start TCP Client");
        System.out.println("3. Start UDP Server");
        System.out.println("4. Start UDP Client");
        System.out.println("5. Exit");
        System.out.print("Choose an Option : ");
        
        int choice = in.nextInt();

        switch(choice) {
            case 1 -> TCPServer.main(new String[0]);
            case 2 -> TCPClient.main(new String[0]);
            case 3 -> UDPServer.main(new String[0]);
            case 4 -> UDPClient.main(new String[0]);
            case 5 -> System.exit(0);

            default -> System.out.println("Invalid Option !");
        }
    }
}
