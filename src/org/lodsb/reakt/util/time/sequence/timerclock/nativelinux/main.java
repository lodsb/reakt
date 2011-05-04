package org.lodsb.lzrcore.linuxclock;

public class main {
   public static void main(String argv[]) {
	 System.out.println("PATH : " + System.getProperty("java.library.path"));
     System.load("linuxclock.so");
     linuxclock.set_ticktime_nanos(1000000-60000);
     for(int i= 0; i < 10000; i++) {
      long start = System.nanoTime();
      linuxclock.next_tick();
      System.out.println(System.nanoTime()-start);
      }
   }
 }
