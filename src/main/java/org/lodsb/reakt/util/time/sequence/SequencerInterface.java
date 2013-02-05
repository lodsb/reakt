/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 4 :: 6 : 56
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

package util.time.sequence;

public interface SequencerInterface {
	//void postSequenceEvent(SequenceEvent sequenceEvent);
	public boolean processTick(long tick);
}
