package net.sf.jtmt.concurrent.actorsguild;

import java.util.concurrent.TimeUnit;

import org.actorsguildframework.AsyncResult;
import org.actorsguildframework.DefaultAgent;

/**
 * ActorsGuild Actor Manager.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ActorManager {
  public static void main(String[] args) {
    DefaultAgent ag = new DefaultAgent();
    
    WriteActor writeActor = ag.create(WriteActor.class);
    IndexActor indexActor = ag.create(IndexActor.class).init(writeActor).get();
    DownloadActor downloadActor = ag.create(DownloadActor.class).init(indexActor).get();

    // The original code allocated an array of AsyncResult[numberOfRequests]
    // and populated it by looping through the number of tasks and seeding the
    // downloadActor with its initial request. Although conceptually simpler, 
    // it needed a huge amount of memory and didn't scale well for 
    // numberOfRequests > 100,000. So the strategy is to batch the tasks
    // into blocks of 100,000 and submit until they are all processed.
    long start = System.nanoTime();
    int numberOfRequests = 1000000;
    int tasksDone = 0;
    while (tasksDone < numberOfRequests) {
      int batchSize = Math.min(numberOfRequests - tasksDone, 100000);
      AsyncResult[] results = new AsyncResult[batchSize];
      for (int i = 0; i < batchSize; i++) {
        results[i] = downloadActor.download(tasksDone + i, "Requested " + i);
      }
      ag.awaitAllUntilError(results);
      tasksDone += batchSize;
    }
    long elapsed = System.nanoTime() - start;
    System.out.println("elapsed=" + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
    ag.shutdown();
  }
  
  public static void log(String message) {
//    System.out.println(message);
  }
}
