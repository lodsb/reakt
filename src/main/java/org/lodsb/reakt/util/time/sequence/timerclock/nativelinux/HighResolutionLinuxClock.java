package util.time.sequence.timerclock.nativelinux;

import java.io.File;

import util.time.sequence.SequencerInterface;
import util.time.sequence.timerclock.ClockInterface;

public class HighResolutionLinuxClock implements ClockInterface, Runnable {

	private SequencerInterface sequencer;

	private long clockStart;

	public HighResolutionLinuxClock(SequencerInterface sequencer) {
		this.sequencer = sequencer;
	}

	public HighResolutionLinuxClock(){}

	//FIXME: fix path
	static {
		String absolutePath = "";
		try {
			// try loading native library from system lib (library path)
			System.loadLibrary("linuxclock");
			System.out.println("native priority library loaded using system library path");
		} catch (Throwable e) {
			try {
				//File file = new File("lib/"+System.getProperty("os.arch")+"/"+System.getProperty("os.name")+"/linuxclock.so");
				File file = new File("libraries/reakt/src/org/lodsb/reakt/util/time/sequence/timerclock/nativelinux/linuxclock.so");

				absolutePath = file.getAbsolutePath();
				System.load(absolutePath);
				System.out.println("loaded priority native library " + file.getAbsolutePath());
			} catch (Throwable e2) {
				System.out.println("Problem loading priority library.");
				System.out.println("Tried system library path and " + absolutePath);
				e.printStackTrace();
				e2.printStackTrace();
			}
		}
	}

	private int numberOfLatencyMeasurements = 10;
	private int numberOfMeanLatencyMeasurements = 10;

	private int currentLatencyMeasurement = 0;
	private int currentMeanLatencyMeasurement = 0;
	private int interval = 0;

	private long currentTick = 0;

	private int[] latencyMeasurements = new int[numberOfLatencyMeasurements];
	private int[] meanLatencyMeasurements = new int[numberOfMeanLatencyMeasurements];

	private void updateLatency(int currentLatency) {

		latencyMeasurements[currentLatencyMeasurement] = currentLatency;

		if (currentLatencyMeasurement == 0) {
			int meanLatency = 0;

			for (long val : latencyMeasurements) {
				meanLatency += val;
			}

			meanLatency = meanLatency / numberOfLatencyMeasurements;

			meanLatencyMeasurements[currentMeanLatencyMeasurement] = meanLatency;

			if (currentMeanLatencyMeasurement == 0) {
				meanLatency = 0;
				//if(currentMeanLatencyMeasurement == 0) {
				for (long val : meanLatencyMeasurements) {
					meanLatency += val;
				}

				meanLatency = meanLatency / numberOfMeanLatencyMeasurements;

				linuxclock.set_ticktime_nanos((int)((interval - meanLatency)));
			}

			//}	

			currentMeanLatencyMeasurement = (currentMeanLatencyMeasurement + 1) % numberOfMeanLatencyMeasurements;

		}

		currentLatencyMeasurement = (currentLatencyMeasurement + 1) % numberOfLatencyMeasurements;
		//System.out.println(currentLatencyMeasurement+" "+currentLatency);
	}

	public int getCurrentMeanLatencyMeasurement() {
		return this.currentMeanLatencyMeasurement;
	}

	@Override
	public boolean setPriority(ProcessType pr, int prio) {
		boolean ret = false;
		switch (pr) {
			case Realtime:
				if (linuxclock.scheduleRealtime(prio) != 0) ret = true;
				break;
			case FIFO:
				if (linuxclock.scheduleFIFO(prio) != 0) ret = true;
				break;
			case Other:
				if (linuxclock.scheduleOther(prio) != 0) ret = true;
				break;
		}

		return ret;
	}

	@Override
	public ProcessType getProcessType() {
		switch (linuxclock.getScheduler()) {
			case 1:
				return ClockInterface.ProcessType.Realtime;
			case 2:
				return ClockInterface.ProcessType.FIFO;
		}

		return ClockInterface.ProcessType.Other;
	}

	@Override
	public int getPriority() {
		return linuxclock.getPriority();
	}

	@Override
	public void setInterval(long intervalNanos) {
		// FIXME: add errorhandling for intervals > maxint
		this.interval = (int) ((double)intervalNanos);
		linuxclock.set_ticktime_nanos((int) intervalNanos);
	}

	@Override
	public void setSequencer(SequencerInterface sequencer) {
		this.sequencer = sequencer;
	}

	@Override
	public void start() {
		Thread clockThread = new Thread(this);
		clockThread.setName("HPClock-rt");
		clockThread.setPriority(Thread.MAX_PRIORITY);
		clockThread.start();
	}

	private int priorityRequested = 80;
	private int priority = 0;

	private boolean run = true;


	public void stopClock() {
		run = false;
	}

	@Override
	public void run() {

		if(this.sequencer == null) {
			System.err.println("[ERROR] No Sequencer set!");
			return;
		}

		if (priorityRequested != priority) {
			System.err.println("[INFO] Clock priority requested " + priorityRequested);

			if (!this.setPriority(ClockInterface.ProcessType.Realtime, priorityRequested)) {
				System.err.println("[Error] Failed to set realtime priority!");
			}
			priority = priorityRequested;
			System.err.println("[INFO] Clock priority is: " + this.getProcessType() + " Prio " + this.getPriority());
		}

		while (run) {
			clockStart = System.nanoTime();
			linuxclock.next_tick();
			sequencer.processTick(currentTick++);
			this.updateLatency((int)((System.nanoTime() - clockStart) - interval));
		}
	}

	static long ticks = 0;
	public static void main(String[] args) {
		HighResolutionLinuxClock c;

		SequencerInterface si = new SequencerInterface() {
			@Override
			public boolean processTick(long tick) {
				ticks++;
				return true;
			}
		};

		c = new HighResolutionLinuxClock(si);

		long start = System.nanoTime();

		c.setInterval(10);

		c.start();


		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long end = System.nanoTime();

		c.stopClock();

		System.out.println(new Double(end-start)/((double) ticks));
	}
}
