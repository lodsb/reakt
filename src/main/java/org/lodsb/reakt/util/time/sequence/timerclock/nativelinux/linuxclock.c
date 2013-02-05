#include <sys/time.h>
#include <stdio.h>
#include <sys/resource.h>
#include <stdlib.h>

#include "pthread.h"

struct timespec req;
struct timespec rem;

struct timeval start_tv;
struct timeval end_tv;

void set_ticktime_nanos(int nanoseconds) {
  long nanos = (long) nanoseconds;
  
  req.tv_sec = 0;
  req.tv_nsec = nanos;
}

void next_tick() {
  nanosleep(&req,&rem);
}


int scheduleRealtime(int prio) {
 struct sched_param sp;

  sp.sched_priority = prio;
    if( sched_setscheduler (0, SCHED_RR , &sp) == 0) {
      	return 1;
    }  else  {
      	return 0;
    }
}

int scheduleFIFO(int prio) {
 struct sched_param sp;

  sp.sched_priority = prio;
    if( sched_setscheduler (0, SCHED_FIFO , &sp) == 0) {
      	return 1;
    }  else  {
      	return 0;
    }
}

int scheduleOther(int prio) {
 struct sched_param sp;

  sp.sched_priority = prio;
    if( sched_setscheduler (0, SCHED_OTHER , &sp) == 0) {
      	return 1;
    }  else  {
      	return 0;
    }
}

int getPriority(void) {
    struct sched_param param;

  int sched = sched_getscheduler(0);
  sched_getparam(0,&param);

  return param.__sched_priority;
}

int getScheduler(void) {
 	int type = sched_getscheduler(0);

 	switch(type) {
 		case SCHED_OTHER: 	return 0;
 		case SCHED_RR: 		return 1;
 		case SCHED_FIFO: 	return 2;
 	}

 	return -1;
}
