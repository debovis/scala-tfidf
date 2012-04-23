package net.sf.jtmt.concurrent.jetlang;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;

/**
 * Jetlang ActorManager.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ActorManager {

  public final static int NUM_ACTORS = 3;
  public final static String STOP = "__STOP__";
  
  public static void main(String[] args) {
    ExecutorService exec = Executors.newCachedThreadPool();
    PoolFiberFactory factory = new PoolFiberFactory(exec);

    // when the poison pill is received, the fiber.dispose() call will
    // call this and decrement the countdown latch. The onstop.await()
    // will block until the latch is zero, so that way the manager waits
    // for all the actors to complete before exiting
    final CountDownLatch onstop = new CountDownLatch(NUM_ACTORS);
    Disposable dispose = new Disposable() {
      public void dispose() {
        onstop.countDown();
      }
    };
    
    Fiber downloadFiber = factory.create();
    downloadFiber.add(dispose);
    DownloadActor downloadActor =  
      new DownloadActor(Channels.downloadChannel, Channels.indexChannel,
          Channels.downloadStopChannel, Channels.indexStopChannel,
          downloadFiber);
    
    Fiber indexFiber = factory.create();
    indexFiber.add(dispose);
    IndexActor indexActor = 
      new IndexActor(Channels.indexChannel, Channels.writeChannel,
          Channels.indexStopChannel, Channels.writeStopChannel,
          indexFiber);
    
    Fiber writeFiber = factory.create();
    writeFiber.add(dispose);
    WriteActor writeActor = 
      new WriteActor(Channels.writeChannel, Channels.writeStopChannel, writeFiber);

    downloadActor.start();
    indexActor.start();
    writeActor.start();
    
    // seed the incoming channel
    long start = System.nanoTime();
    int numTasks = 1000000;
    for (int i = 0; i < numTasks; i++) {
      String payload = "Requested " + i;
      log(payload);
      Channels.downloadChannel.publish(payload);
    }
    // send the poison pill to stop processing
    Channels.downloadStopChannel.publish(null);
    
    try { 
      onstop.await(); 
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    long elapsed = System.nanoTime() - start;
    System.out.println("elapsed=" + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
    exec.shutdown();
  }
  
  public static void log(String message) {
//    System.out.println(message);
  }
}
