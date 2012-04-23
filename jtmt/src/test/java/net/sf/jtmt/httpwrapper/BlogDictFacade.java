// $Id$
package net.sf.jtmt.httpwrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal 
 * @version $Revision$
 */
public class BlogDictFacade {

  private BlogDict blogDict;
  
  public BlogDictFacade(String file) {
    this.blogDict = new BlogDict(file);
  }
  
  protected void init() { /* nothing to do here */ }
  
  protected void destroy() { /* nothing to do here */ }
  
  public void getLabels(HttpServletRequest request, HttpServletResponse response) {
    Set<String> labels = blogDict.getLabels();
    response.setContentType("application/x-javascript");
    try {
      PrintWriter responseWriter = response.getWriter();
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(responseWriter, labels);
      responseWriter.flush();
      responseWriter.close();
    } catch (IOException e) {
      Joh.error(e, request, response);
    }
  }
  
  public void getSynonyms(HttpServletRequest request, HttpServletResponse response) {
    Map<String,String> parameters = Joh.getParameters(request);
    if (parameters.containsKey("label")) {
      Set<String> synonyms = blogDict.getSynonyms(parameters.get("label"));
      response.setContentType("application/x-javascript");
      try {
        PrintWriter responseWriter = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(responseWriter, synonyms);
        responseWriter.flush();
        responseWriter.close();
      } catch (IOException e) {
        Joh.error(e, request, response);
      }
    } else {
      Joh.error(new Exception("Parameter 'label' not provided"), request, response);
    }
  }
  
  public void getCategories(HttpServletRequest request, HttpServletResponse response) {
    Map<String,String> parameters = Joh.getParameters(request);
    if (parameters.containsKey("label")) {
      Set<String> categories = blogDict.getCategories(parameters.get("label"));
      response.setContentType("application/x-javascript");
      try {
        PrintWriter responseWriter = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(responseWriter, categories); 
        responseWriter.flush();
        responseWriter.close();
      } catch (IOException e) {
        Joh.error(e, request, response);
      }
    } else {
      Joh.error(new Exception("Parameter 'label' not provided"), request, response);
    }
  }
  
  public static void main(String[] argv) throws Exception {
    Map<String,Object> config = new HashMap<String,Object>();
    config.put(Joh.HTTP_PORT_KEY, new Integer(8080));
    Joh.expose(new BlogDictFacade("/home/sujit/bin/blog_dict.txt"), config);
  }
}
