package org.lodsb.lzrcore;

public interface SequencerInterface {
	void postSequenceEvent(SequenceEvent sequenceEvent);
	public boolean processTick(long tick);
}
