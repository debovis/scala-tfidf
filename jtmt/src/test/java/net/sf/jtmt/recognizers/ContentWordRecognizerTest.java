package net.sf.jtmt.recognizers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import net.sf.jtmt.tokenizers.Token;
import net.sf.jtmt.tokenizers.WordTokenizer;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Test harness for testing the functionality of ContentWordRecognizer.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ContentWordRecognizerTest {

  private static RecognizerChain chain;
  
  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    DataSource dataSource = new DriverManagerDataSource(
        "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/tmdb", "root", "orange");
      chain = new RecognizerChain(Arrays.asList(new IRecognizer[] {
        new BoundaryRecognizer(),
        new AbbreviationRecognizer(dataSource),
        new PhraseRecognizer(dataSource),
        new ContentWordRecognizer()
      }));
      chain.init();
  }
  
  @Test
  public void testIndexSamples() throws Exception {
    BufferedReader reader = new BufferedReader(
      new FileReader("src/test/resources/data/indexing_sample_data.txt"));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] parts = StringUtils.split(line, ";");
      WordTokenizer wordTokenizer = new WordTokenizer();
      wordTokenizer.setText(parts[1]);
      Token token = null;
      List<Token> tokens = new ArrayList<Token>();
      while ((token = wordTokenizer.nextToken()) != null) {
        tokens.add(token);
      }
      System.out.println("Input sentence:" + line);
      List<Token> recognizedTokens = chain.recognize(tokens);
      for (Token recognizedToken : recognizedTokens) {
        System.out.println(">> Token=" + recognizedToken.getValue() + " [" + recognizedToken.getType() + "]");
      }
    }
  }
}
