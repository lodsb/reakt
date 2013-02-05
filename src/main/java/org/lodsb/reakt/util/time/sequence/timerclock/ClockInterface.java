/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 4 :: 6 : 45
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

package util.time.sequence.timerclock;
import util.time.sequence.SequencerInterface;

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
