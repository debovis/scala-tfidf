package net.sf.jtmt.concurrent.kilim;

import java.util.concurrent.TimeUnit;

import kilim.ExitMsg;
import kilim.Mailbox;

/**
 * Kilim Actor Manager.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ActorManager {

  public static final String STOP = "__STOP__";
  private static final int ACTOR_THREAD_POOL_SIZE = 2;
  
  public static void main(String[] args) {
    Mailbox<String> mb0 = new Mailbox<String>();
    Mailbox<String> mb1 = new Mailbox<String>();
    Mailbox<String> mb2 = new Mailbox<String>();
    Mailbox<ExitMsg> callback = new Mailbox<ExitMsg>();
    
    // instantiate actors
    DownloadActor downloadActor = new DownloadActor(ACTOR_THREAD_POOL_SIZE, mb0, mb1);
    IndexActor indexActor = new IndexActor(ACTOR_THREAD_POOL_SIZE, mb1, mb2);
    WriteActor writeActor = new WriteActor(ACTOR_THREAD_POOL_SIZE, mb2, null);
    
    // start the actors
    downloadActor.start();
    indexActor.start();
    writeActor.start();
    writeActor.informOnExit(callback);
    
    long start = System.nanoTime();
    int numTasks = 1000000;
    for (int i = 0; i < numTasks; i++) {
      String req = "Requested " + i;
      mb0.putnb(req);
      log(req);
    }
    
    // poison pill to stop the actors
    mb0.putnb(ActorManager.STOP);
    // block till the last actor has informed the manager that it exited
    callback.getb();
    long elapsed = System.nanoTime() - start;
    System.out.println("elapsed=" + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
    System.exit(0);
  }
  
  public static void log(String message) {
//    System.out.println(message);
  }
}
