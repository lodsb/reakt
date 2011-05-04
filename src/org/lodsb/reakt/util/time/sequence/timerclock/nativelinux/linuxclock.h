void set_ticktime_nanos(int nanoseconds);
void next_tick();

int scheduleRealtime(int prio);
int scheduleFIFO(int prio);
int scheduleOther(int prio);

int getScheduler(void);
int getPriority(void);
