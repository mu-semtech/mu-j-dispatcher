package com.tenforce.mu_semtech.mu_j_dispatcher;

import java.net.*;
import java.io.*;
/*
    The Main entry class.

    Calling this function will start a proxy that will listen on the specified
    port or if no port was specified it will lilsten on port 80.

    The idiomatic usage of this class is:
    java com.tenforce.mu_semtech.mu_j_dispatcher.Proxy [PORT]
*/
public class Proxy {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        boolean listening = true;

        int port = 80;

        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("com.tenforce.mu_semtech.mu_j_dispatcher.Proxy server started on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        } catch (IOException e) {
            System.err.println("Error starting proxy on port: " + port);
            e.printStackTrace();
            System.exit(-1);
        }

        while (listening) {
            new DispatchThread(serverSocket.accept()).start();
        }
        serverSocket.close();
    }
}
