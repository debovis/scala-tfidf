package net.sf.jtmt.concurrent.actorfoundry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import osl.manager.Actor;
import osl.manager.ActorName;
import osl.manager.RemoteCodeException;
import osl.manager.annotations.message;

/**
 * ActorFoundry manager.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ActorManager extends Actor {
  
  private static final long serialVersionUID = -8621318190754146319L;

  private static final CountDownLatch latch = new CountDownLatch(3);
  
  @message
  public void boot(Integer tasks) {
    try {
      ActorName downloadActor = create(DownloadActor.class, self());
      // seed the download actor with numRequests tasks
      long start = System.nanoTime();
      for (int i = 0; i < tasks; i++) {
        String message = "Requested " + i;
        send(downloadActor, "download", message);
//        send(stdout, "println", message);
      }
      // send poison pill to terminate actors
      send(downloadActor, "stop");
      // wait for all the actors to terminate after getting the poison pill
      latch.await();
      long elapsed = System.nanoTime() - start;
      send(stdout, "println", "Time elapsed=" + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
      System.exit(0);
    } catch (RemoteCodeException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  @message
  public static void decrementLatch() {
    latch.countDown();
  }
}
