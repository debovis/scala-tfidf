// $Id$
package net.sf.jtmt.httpwrapper;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Simple Jetty HTTP Client to test our Joh enabled BlogDict.
 * @author Sujit Pal
 * @version $Revision$
 */
public class JohClient {

  public Object request(String url, Class<?> clazz) {
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(url);
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
      new DefaultHttpMethodRetryHandler(3, false));
    try {
      int status = client.executeMethod(method);
      if (status != HttpStatus.SC_OK) {
        throw new Exception(method.getStatusText() + 
          " [" + method.getStatusCode() + "]");
      }
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(method.getResponseBodyAsStream(), clazz);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      method.releaseConnection();
    }
  }
}
