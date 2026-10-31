package audio;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class munt {
  
    private final Sequencer sequencer;
    private final MidiDevice muntDevice;
    private final Receiver muntReceiver;
    private final List<File> playlist = new ArrayList<>();
    private int currentIndex = -1;

    public munt() throws MidiUnavailableException {
        MidiDevice.Info muntInfo = null;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (info.getName().toLowerCase().contains("mt-32") ||
                info.getName().toLowerCase().contains("mt32")) {
                muntInfo = info;
            }
        }
        if (muntInfo == null)
            throw new MidiUnavailableException("Munt not found");

        muntDevice = MidiSystem.getMidiDevice(muntInfo);
        muntDevice.open();
        muntReceiver = muntDevice.getReceiver();

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.getTransmitter().setReceiver(muntReceiver);
    }

    public void addFile(File f) {
        if (f.exists()) playlist.add(f);
    }

    public void playMidi(int index) {
		if (index < 0 || index >= playlist.size()) return;
		// Fade out over ~300ms
		for (int vol = 127; vol >= 0; vol -= 16) {
			try {
				for (int ch = 1; ch <= 15; ch++)
					muntReceiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 7, vol), -1);
				Thread.sleep(50);
			} catch (Exception e) { break; }
		}

		currentIndex = index;
		try {
			sequencer.stop();
			sequencer.setSequence(MidiSystem.getSequence(playlist.get(currentIndex)));
			sequencer.start();

			// Fade in
			for (int vol = 0; vol <= 127; vol += 16) {
				for (int ch = 1; ch <= 15; ch++)
					muntReceiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 7, vol), -1);
				Thread.sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}   

    public void play() {
        if (currentIndex >= 0 && sequencer.isRunning())
            sequencer.start();
    }

    public void pause() {
        if (!sequencer.isRunning())
            sequencer.stop();
    }

    public void next() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        playMidi(currentIndex);
    }

    public void previous() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playMidi(currentIndex);
    }

    public void playSoundEffect(int channel, int note, int velocity, int durationMs) {
        new Thread(() -> {
            try {
                muntReceiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, note, velocity), -1);
                Thread.sleep(durationMs);
                muntReceiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel, note, 0), -1);
            } catch (Exception e) { }
        }).start();
    }

    public void shutdown() {
        sequencer.close();
        muntDevice.close();
    }
}