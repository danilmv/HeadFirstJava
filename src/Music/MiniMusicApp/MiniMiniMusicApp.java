package Music.MiniMusicApp;

import javax.sound.midi.*;

public class MiniMiniMusicApp {
    public static void main(String[] args) {
        MiniMiniMusicApp app = new MiniMiniMusicApp();
        app.play();
    }

    public void play() {
        Sequencer player = null;
        try {
            //get a Sequencer and open it
            player = MidiSystem.getSequencer();
            player.open();

            Sequence seq = new Sequence(Sequence.PPQ, 4);

            Track track = seq.createTrack();

            ShortMessage a = new ShortMessage();
            //what to do: start playing note 44
            //144 - command for "NOTE ON" ~ start playing, 128 = "NOTE OFF" ~ stop playing, 192 - change instrument
            //channel 1 ... we can set which instrument plays on different channels and which one should play now
            //44 - note number (from 0 to 127)
            //100 - velocity
            a.setMessage(144,1,44,100);
            //when to do it: trigger message 'a' at first beat
            MidiEvent noteOn = new MidiEvent(a, 1);
            //a track holds all the MidiEvent objects. You can have lots of events happening at the ame moment in time
            track.add(noteOn);

            ShortMessage secondChannel = new ShortMessage();
            //change default instrument of channel 2 t #102
            secondChannel.setMessage(192, 2, 51, 0);
            MidiEvent setChannel2 = new MidiEvent(secondChannel, 5);
            track.add(setChannel2);

            ShortMessage playSecondChannel = new ShortMessage();
            //play note on channel 2
            playSecondChannel.setMessage(144, 2, 44, 100);
            track.add(new MidiEvent(playSecondChannel, 5));

            ShortMessage b = new ShortMessage();
            b.setMessage(128,1,44,100);
            MidiEvent noteOff = new MidiEvent(b, 16);
            track.add(noteOff);

            ShortMessage stopSecondChannel = new ShortMessage();
            stopSecondChannel.setMessage(128, 2, 44,100);
            track.add(new MidiEvent(secondChannel, 24));

            player.setSequence(seq);

            player.start();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
}
