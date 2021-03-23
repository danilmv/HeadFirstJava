package Sockets;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        new Server();
    }

    private ServerSocket server;
    private ArrayList<ObjectOutputStream> clients = new ArrayList<>();

    public Server() {
        try {
            server = new ServerSocket(5000);
            Thread serverThread = new Thread(() -> {
                startListen();
            });
            serverThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ConnectedClient implements Runnable {
        private Socket socket;
        private ObjectInputStream in;

        public ConnectedClient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("server: Thread has started");
            Object objectIn = null;

            if (in == null)
                try {
                    in = new ObjectInputStream(socket.getInputStream());
                } catch (IOException e) {
                    System.out.println("server.ConnectedClient: (error)" + e.getMessage());
                }

            try {
                while ((objectIn = in.readObject()) != null) {
                    System.out.println("server: client has sent a message");

                    sendEveryOne(objectIn);
                }
            } catch (Exception e) {
                System.out.println("server: (error) " + e.getMessage());
            }
        }
    }

    private void startListen() {
        try {
            while (true) {
                Socket client = server.accept();

                System.out.println("server: a client tries to connect");

                //сохраняем ObjectOutputStream клиента для возможности отправки сообщений всем клиентам
                clients.add(new ObjectOutputStream(client.getOutputStream()));

                //начинаем прослушивать ObjectInputStream клиента в новом потоке
                Thread clientThread = new Thread(new ConnectedClient(client));
                clientThread.start();

                System.out.println("server: connection established");
            }
        } catch (IOException e) {
            System.out.println("server: (error)" + e.getMessage());
        }
    }

    //    private void sendEveryOne(byte[] bytes) {
    private void sendEveryOne(Object object) {
        for (ObjectOutputStream os : clients) {
            try {
                System.out.println("server: sending received message to clients...");// + bytes.length + " bytes");
                os.writeObject(object);
//                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
