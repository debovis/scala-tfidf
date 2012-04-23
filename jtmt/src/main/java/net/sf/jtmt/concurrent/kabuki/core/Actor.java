// $Id$
// $Source$
package net.sf.jtmt.concurrent.kabuki.core;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Superclass that all Actors implementations must extend.
 * @author Sujit Pal 
 * @version $Revision$
 */
public abstract class Actor<I,O> {

  public static enum Type {
    READ_ONLY, WRITE_ONLY, READ_WRITE
  };
  
  public static final String SHUTDOWN_COMMAND = "SHUTDOWN";

  private static final String PAYLOAD_KEY = "payload";
  private static final String COMMAND_KEY = "command";
  private static final long SHUTDOWN_DELAY = 5000; // 5s
  
  protected final Logger logger = Logger.getLogger(getClass());
  
  private String brokerUrl;
  private String inbox;
  private String outbox;
  
  private Connection connection;
  // payload is p2p - since we want only one actor at a tier to pick
  // up the incoming payload
  private Session payloadSession;
  private Queue payloadInbox;
  private Queue payloadOutbox;
  private MessageConsumer payloadReceiver;
  private MessageProducer payloadSender;
  // command is pub-sub, since we want all actors in a tier to pick
  // up the incoming command
  private Session commandSession;
  private Topic commandTopic;
  private MessageConsumer commandSubscriber;
  private MessageProducer commandPublisher;
  
  public abstract Type type();
  public abstract void init() throws Exception;
  public abstract O perform(I input) throws Exception;
  public abstract void destroy() throws Exception;

  public void setBrokerUrl(String brokerUrl) {
    this.brokerUrl = brokerUrl;
  }
  
  public void setInbox(String inbox) {
    this.inbox = inbox;
  }
  
  public void setOutbox(String outbox) {
    this.outbox = outbox;
  }
  
  protected final void start() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
    connection = factory.createConnection();
    commandSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    payloadSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    // set up payload queue destinations and friends
    payloadInbox = payloadSession.createQueue(
      StringUtils.join(new String[] {PAYLOAD_KEY, inbox}, "."));
    payloadReceiver = payloadSession.createConsumer(payloadInbox);
    payloadReceiver.setMessageListener(new PayloadListener());
    if (type() == Type.WRITE_ONLY || type() == Type.READ_WRITE) {
      payloadOutbox = payloadSession.createQueue(
        StringUtils.join(new String[] {PAYLOAD_KEY, outbox}, "."));
      payloadSender = payloadSession.createProducer(payloadOutbox);
    }
    // set up command topic destination and friends
    commandTopic = commandSession.createTopic(
      StringUtils.join(new String[] {
      COMMAND_KEY, getClass().getSimpleName()}, ".")); 
    commandSubscriber = commandSession.createConsumer(commandTopic);
    commandSubscriber.setMessageListener(new CommandListener());
    commandPublisher = commandSession.createProducer(commandTopic);
    // start your engine
    connection.start();
    logger.info("Actor Started: " + getClass().getName());
    // call the init() hook
    init();
  }

  protected void shutdown() throws Exception {
    if (payloadOutbox != null) {
      MapMessage shutdownMessage = payloadSession.createMapMessage();
      shutdownMessage.setString(COMMAND_KEY, SHUTDOWN_COMMAND);
      payloadSender.send(shutdownMessage);
    }
    if (type() == Type.WRITE_ONLY) {
      logger.info("Waiting " + SHUTDOWN_DELAY + "ms...");
      try { Thread.sleep(SHUTDOWN_DELAY); }
      catch (InterruptedException e) { /* NOOP */ }
      stop();
    }
  }

  protected final void send(O output) throws Exception {
    if (type() != Type.WRITE_ONLY) {
      throw new IllegalAccessError(
        "send() can only be called from WRITE_ONLY Actors");
    }
    if (payloadSender != null) {
      MapMessage payload = payloadSession.createMapMessage();
      payload.setObject(PAYLOAD_KEY, output);
      payloadSender.send(payload);
    }
  }

  private final void stop() throws Exception {
    closeQuietly(payloadReceiver);
    closeQuietly(commandSubscriber);
    closeQuietly(payloadSender);
    closeQuietly(commandPublisher);
    commandSession.close();
    payloadSession.close();
    connection.close();
    // call the destroy hook
    destroy();
    logger.info("Actor stopped: " + getClass().getName());
  }

  private void closeQuietly(MessageProducer producer) {
    if (producer != null) {
      try { producer.close(); }
      catch (JMSException e) { /* NOOP */ }
    }
  }
  
  private void closeQuietly(MessageConsumer consumer) {
    if (consumer != null) {
      try { consumer.close(); }
      catch (JMSException e) { /* NOOP */ }
    }
  }
  
  private final class PayloadListener implements MessageListener {
    @SuppressWarnings("unchecked")
    @Override public void onMessage(Message message) {
      try {
        MapMessage payload = (MapMessage) message;
        // check to see if this is a shutdown message. If so, send to
        // the command topic for this actor 
        String command = (String) payload.getObject(COMMAND_KEY);
        if (SHUTDOWN_COMMAND.equals(command)) {
          // send the shutdown command to the command topic
          TextMessage shutdownMessage = commandSession.createTextMessage();
          shutdownMessage.setText(SHUTDOWN_COMMAND);
          commandPublisher.send(shutdownMessage);
          // pass it on to the next tier
          shutdown();
        } else {
          I input = (I) payload.getObject(PAYLOAD_KEY);
          O output = null;
          if (input != null) {
            try { 
              output = perform(input);
            } catch (Exception e) {
              logger.error(e);
            }
          }
          if (output != null && payloadSender != null) {
            MapMessage outputPayload = payloadSession.createMapMessage();
            outputPayload.setObject(PAYLOAD_KEY, output);
            payloadSender.send(outputPayload);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  };

  private final class CommandListener implements MessageListener {
    @Override public void onMessage(Message message) {
      try {
        TextMessage command = (TextMessage) message;
        if (SHUTDOWN_COMMAND.equals(command.getText())) {
          stop();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  };

  // these methods are called from the shell script to start up an instance
  // of an Actor.
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    CommandLineParser clp = new BasicParser();
    Options options = new Options();
    options.addOption("h", "help", false, "Print this message");
    options.addOption("a", "actor", true, "Full class name for Actor");
    options.addOption("i", "input", true, "Inbox");
    options.addOption("o", "output", true, "Outbox (optional for READ_ONLY)");
    options.addOption("u", "brokerUrl", true, "ActiveMQ Broker URL");
    CommandLine cl = clp.parse(options, args);
    if (cl.hasOption("h")) {
      printUsage(null, options);
      System.exit(0);
    }
    String actorClassName = null;
    if (cl.hasOption("a")) {
      actorClassName = cl.getOptionValue("a");
    }
    String brokerUrl = null;
    if (cl.hasOption("u")) {
      brokerUrl = cl.getOptionValue("u");
    }
    String inputAlias = null;
    if (cl.hasOption("i")) {
      inputAlias = cl.getOptionValue("i");
    }
    String outputAlias = null;
    if (cl.hasOption("o")) {
      outputAlias = cl.getOptionValue("o");
    }
    // validation: broker url must be defined
    if (StringUtils.isEmpty(brokerUrl)) {
      printUsage("Broker URL must be defined", options);
    }
    // validation: actor class name must be defined
    if (StringUtils.isEmpty(brokerUrl)) {
      printUsage("Actor class must be defined", options);
    }
    Actor actor = 
      (Actor) Class.forName(actorClassName).newInstance();
    // validation: 
    // all actors must have a payload inbox
    // only read-only actors may or may not have a payload outbox
    if (StringUtils.isEmpty(inputAlias)) {
      printUsage("No Inbox specified for actor:" + actorClassName, options);
    }
    if (StringUtils.isEmpty(outputAlias)) {
      if (actor.type() != Type.READ_ONLY) {
        printUsage("No Outbox specified for actor:" + actorClassName, options);
      }
    }
    actor.setBrokerUrl(brokerUrl);
    actor.setInbox(inputAlias);
    actor.setOutbox(outputAlias);
    actor.start();
  }

  private static void printUsage(String message, Options options) {
    if (StringUtils.isNotEmpty(message)) {
      System.out.println("ERROR: " + message);
    }
    HelpFormatter formatter = new HelpFormatter();
    formatter.defaultWidth = 80;
    formatter.printHelp("java " + Actor.class.getName() + 
      " [-h|-a class -u url -i input [-o output]]", options);
  }
}
