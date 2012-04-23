package net.sf.jtmt.misc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Converts the co_colloc data from TextMine to something more suitable
 * for my application. This has been copied from PhraseRecognizer.init(),
 * needs to be revisited for BDB in-memory databases.
 * @author Sujit Pal
 * @version $Revision$
 */
public class TextMineCollocConverterTest {

  private final Log log = LogFactory.getLog(getClass());
  
  private JdbcTemplate jdbcTemplate;

  @Test
  public void convert() throws Exception {
    DataSource dataSource = new DriverManagerDataSource(
        "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/tmdb", "root", "orange");
    jdbcTemplate = new JdbcTemplate(dataSource);
    List<Map<String,String>> rows = jdbcTemplate.queryForList(
    "select coc_lead_word, coc_words from co_colloc");
    for (Map<String,String> row : rows) {
      String leadWord = row.get("COC_LEAD_WORD");
      String phraseCompletions = row.get("COC_WORDS");
      phraseCompletions = phraseCompletions.replaceAll("\\|\\|\\|(\\d+)\\s", "\\|\\|\\|$1, ");
      String[] completions = phraseCompletions.split("\\,\\s+");
      for (String completion : completions) {
        // throw away non-words
        if (completion.contains("|||")) {
          String[] parts = StringUtils.split(completion, "|||");
          if (Character.isLetterOrDigit(parts[0].charAt(0))) {
            try {
              log.debug("insert=" + leadWord + "|" + parts[1] + "|" + parts[0]);
              int numTokens = Integer.valueOf(parts[1]);
              jdbcTemplate.update(
                "insert into my_colloc(coc_lead_word, coc_phrase, coc_num_words, coc_prob) values (?,?,?,?)",
                new Object[] {leadWord, parts[0], Integer.valueOf(parts[1]), new Float(0.0F)});
            } catch (Exception e) {
              log.error("Exception caught, failed insert", e);
            }
          }
        }
      }
    }
  }
}
