/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package util.time.sequence.timerclock.nativelinux;

class linuxclockJNI {
  public final static native void set_ticktime_nanos(int jarg1);
  public final static native void next_tick();
  public final static native int scheduleRealtime(int jarg1);
  public final static native int scheduleFIFO(int jarg1);
  public final static native int scheduleOther(int jarg1);
  public final static native int getScheduler();
  public final static native int getPriority();
}
