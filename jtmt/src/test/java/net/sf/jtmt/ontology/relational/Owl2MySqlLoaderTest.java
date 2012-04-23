package net.sf.jtmt.ontology.relational;

import javax.sql.DataSource;

import net.sf.jtmt.ontology.relational.daos.EntityDao;
import net.sf.jtmt.ontology.relational.daos.FactDao;
import net.sf.jtmt.ontology.relational.daos.RelationDao;
import net.sf.jtmt.ontology.relational.loaders.Owl2MysqlLoader;

import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Harness for running OWL Parser.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Owl2MySqlLoaderTest {
  
  @Test
  public void testParseAndLoadData() throws Exception {
    DataSource dataSource = new DriverManagerDataSource(
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/ontodb", "root", "orange");
    
    EntityDao entityDao = new EntityDao();
    entityDao.setDataSource(dataSource);
    
    RelationDao relationDao = new RelationDao();
    relationDao.setDataSource(dataSource);
    
    FactDao factDao = new FactDao();
    factDao.setEntityDao(entityDao);
    factDao.setRelationDao(relationDao);
    factDao.setDataSource(dataSource);
    
    Owl2MysqlLoader loader = new Owl2MysqlLoader();
    loader.setEntityDao(entityDao);
    loader.setFactDao(factDao);
    loader.setOwlFileLocation("src/main/resources/wine.rdf");
    
    loader.parseAndLoadData();
  }

}
