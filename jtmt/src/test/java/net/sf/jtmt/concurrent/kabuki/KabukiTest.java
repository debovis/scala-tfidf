// $Id$
// $Source$
package net.sf.jtmt.concurrent.kabuki;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Test for Kabuki.
 * @author Sujit Pal
 * @version $Revision$
 */
public class KabukiTest {

  public void test() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
      "tcp://localhost:61616");
    Connection conn = factory.createConnection();
    Session statSession = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session testSession = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    conn.start();

    Queue inbox = statSession.createTemporaryQueue();
    MessageConsumer con = statSession.createConsumer(inbox);

    Queue testQueue = testSession.createQueue("payload.index");
    Queue statQueue = statSession.createQueue("ActiveMQ.Statistics.Destination.payload.index");
    MessageProducer testPro = statSession.createProducer(testQueue);
    Message testMsg = testSession.createMessage();
    testPro.send(testMsg);
    
    Message statMsg = statSession.createMessage();
    statMsg.setJMSReplyTo(inbox);
    MessageProducer statPro = statSession.createProducer(statQueue);
    statPro.send(statMsg);

    MapMessage reply = (MapMessage) con.receive();
    
    for (Enumeration e = reply.getMapNames(); e.hasMoreElements();) {
      String key = e.nextElement().toString();
      Object val = reply.getObject(key);
      System.out.println(key + " => " + val);
    }
    
    con.close();
    testPro.close();
    statPro.close();
    statSession.close();
    testSession.close();
    conn.close();
  }
  
  public static void main(String[] args) {
    try {
      KabukiTest sg = new KabukiTest();
      sg.test();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
