// $Id$
package net.sf.jtmt.httpwrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections15.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;

/**
 * Main class invoked by caller.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Joh {

  private final static Log LOG = LogFactory.getLog(Joh.class);
  
  public final static String HTTP_PORT_KEY = "_http_port";
  
  private final static int DEFAULT_HTTP_PORT = 8080;
  
  public static void expose(Object obj, Map<String,Object> config)
      throws Exception {
    Server server = new Server();
    Connector connector = new SocketConnector();
    if (config != null && config.containsKey(HTTP_PORT_KEY)) {
      connector.setPort((Integer) config.get(HTTP_PORT_KEY));
    } else {
      connector.setPort(DEFAULT_HTTP_PORT);
    }
    server.setConnectors(new Connector[] {connector});
    Handler handler = new JohHandler(obj);
    server.setHandler(handler);
    server.start();
    server.join();
  }
  
  public static Map<String,String> getParameters(HttpServletRequest request) {
    Map<String,String> parameters = new CaseInsensitiveMap<String>();
    Map<String,String[]> params = request.getParameterMap();
    for (String key : params.keySet()) {
      parameters.put(key, StringUtils.join(params.get(key), ","));
    }
    return parameters;
  }

  public static void error(Exception e, HttpServletRequest request,
      HttpServletResponse response) {
    response.setContentType("text/html");
    try {
      PrintWriter responseWriter = response.getWriter();
      responseWriter.println("<html><head><title>Error Page</title></head>");
      responseWriter.println("<body><font color=\"red\">");
      e.printStackTrace(responseWriter);
      responseWriter.println("</font></body></html>");
      responseWriter.flush();
      responseWriter.close();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      LOG.error(e);
    } catch (IOException ioe) {
      LOG.error(ioe);
    }
  }
}
