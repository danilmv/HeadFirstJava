package Music.BeatBox;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<>(256);

    private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    private boolean exitOnClose = true;

    private JFrame frame;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public BeatBox() {
    }

    public BeatBox(boolean exitOnClose) {
        this.exitOnClose = exitOnClose;
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        BeatBox app = new BeatBox();
        app.startBeatBox(true);
//        app.buildGUI();
//        app.setUpMidi();
//
//        app.loadData();
    }

    public void startBeatBox(boolean loadData) {
        this.buildGUI();
        this.setUpMidi();

        if (loadData)
            this.loadData();
    }

    public void buildGUI() {
        frame = new JFrame("Cyber BeatBox");
        frame.setLocationRelativeTo(null);
        if (exitOnClose)
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                saveData();
            }
        });

        frame.setLayout(new BorderLayout());
        ((JPanel) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(e -> buildTrackAndStart());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(e -> sequencer.stop());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(e -> sequencer.setTempoFactor(sequencer.getTempoFactor() * 1.03f));
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(e -> sequencer.setTempoFactor(sequencer.getTempoFactor() * 0.97f));
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < instruments.length; i++) {
            nameBox.add(new Label(instrumentNames[i])); //JLabel?
        }

        frame.add(buttonBox, BorderLayout.EAST);
        frame.add(nameBox, BorderLayout.WEST);


        GridLayout gridLayout = new GridLayout(16, 16);
        gridLayout.setVgap(1);
        gridLayout.setHgap(2);
        JPanel checkBoxPanel = new JPanel(gridLayout);
        frame.add(checkBoxPanel, BorderLayout.CENTER);

        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (MidiUnavailableException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        int[] trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox checkBox = (JCheckBox) checkBoxes.get(j + 16 * i);
                if (checkBox.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int command, int channel, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, one, two);
            event = new MidiEvent(message, tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return event;
    }

    public void saveData() {
        boolean[] data = new boolean[checkBoxes.size()];

        try (FileOutputStream fos = new FileOutputStream("BeatBox.last")) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                for (int i = 0; i < checkBoxes.size(); i++)
                    data[i] = checkBoxes.get(i).isSelected();

                oos.writeObject(data);
            }
//            exportData(fos); //for Socket we need separate OOS...
        } catch (IOException e) {
            System.out.println("MusicPlayer: (error @saveData)" + e.getMessage());
        }
    }

    public void exportData(OutputStream os) {
        boolean[] data = new boolean[checkBoxes.size()];
//        try (ObjectOutputStream oos = new ObjectOutputStream(os)) { >> следует вызов oos.close и он закрывает сокет
        try {
            if (oos == null)
                oos = new ObjectOutputStream(os);

            for (int i = 0; i < checkBoxes.size(); i++)
                data[i] = checkBoxes.get(i).isSelected();

            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importData(InputStream is) {
        boolean[] data = null;
        try {
            if (ois == null) //we need to have only one ObjectInputStream
                ois = new ObjectInputStream(is);

            data = (boolean[]) ois.readObject();

            for (int i = 0; i < data.length; i++)
                checkBoxes.get(i).setSelected(data[i]);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void importObject(Object object) {
        boolean[] data = null;

        if (object == null)
            return;

        data = (boolean[]) object;
        for (int i = 0; i < data.length; i++)
            checkBoxes.get(i).setSelected(data[i]);
    }

    public void loadData() {
        boolean[] data = null;
        try {
            FileInputStream fis = new FileInputStream("BeatBox.last");
//            importData(fis); //for Socket we need to create only one OIS, so for file we'll use separate logic
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                data = (boolean[]) ois.readObject();

                for (int i = 0; i < data.length; i++)
                    checkBoxes.get(i).setSelected(data[i]);
            } catch (ClassNotFoundException| ClassCastException e) {
                System.out.println("MusicPlayer: (error @loadData)" + e.getMessage());;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
