package org.lodsb.lzrcore;

import com.sun.media.sound.RealTimeSequencerProvider;

public interface ClockInterface {
	public enum ProcessType {
		Realtime,
		FIFO,
		Other,
	}

	public boolean setPriority(ProcessType pr, int prio);
	public ProcessType getProcessType();
	public int getPriority();


	public void setInterval(long interval);
	public void setSequencer(SequencerInterface sequencer);
	public void start();
	public void stopClock();
}
