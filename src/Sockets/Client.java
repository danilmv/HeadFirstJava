package Sockets;

import Music.BeatBox.BeatBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Client {
    private static int ports = 4242;
    private OutputStream out;
    private ObjectInputStream in;
    private InputStream is;
    private Socket socket;

    private BeatBox musicPlayer = new BeatBox(false);

    private Object importedData;

    private String name;

    public static void main(String[] args) {
        new Server();

        new Client("Client-1");
        new Client("Client-2");
    }

    public Client(String name) {
        this.name = name;

        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridLayout(1, 4));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setSize(500, 100);

        JButton showPlayer = new JButton("Show Player");
        showPlayer.addActionListener(e -> {
            musicPlayer.show();
        });
        mainFrame.add(showPlayer);

        JButton connectToServer = new JButton("Connect");
        connectToServer.addActionListener(e -> {
            connect();
        });
        mainFrame.add(connectToServer);

        JButton sendMusic = new JButton("Send Music");
        sendMusic.addActionListener(e -> {
            System.out.println(name + ": sending music to the server");
            musicPlayer.exportData(out);
        });
        mainFrame.add(sendMusic);

        JButton loadMusic = new JButton("Load Music");
        loadMusic.addActionListener(e -> {
            System.out.println(name + ": loading imported music to a player");
            musicPlayer.importObject(importedData);
        });
        mainFrame.add(loadMusic);

        musicPlayer.startBeatBox(true);

        mainFrame.setVisible(true);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                musicPlayer.saveData();
            }
        });
    }

    public void connect() {
        try {
            if (socket != null && !socket.isClosed())
                return;

            socket = new Socket("127.0.0.1", 5000);
            System.out.println(name + ": we have successfully connected to a server");

            out = socket.getOutputStream();

            is = socket.getInputStream();
            in = new ObjectInputStream(is);

            Thread connectionThread = new Thread(() -> {
                Object object = null;

                    try {
                        while ((object = in.readObject()) != null) {
                            System.out.println(name + ": we have received a message");
                            importedData = object;
                        }
                    } catch (Exception e) {
                        System.out.println(name + ": (error)" + e.getMessage());
                        return;
                    }
            });

            connectionThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
