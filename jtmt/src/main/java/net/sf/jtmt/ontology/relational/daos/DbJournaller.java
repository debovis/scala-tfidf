package net.sf.jtmt.ontology.relational.daos;

import java.util.Date;

import javax.sql.DataSource;

import net.sf.json.JSONObject;
import net.sf.jtmt.ontology.relational.Attribute;
import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Fact;
import net.sf.jtmt.ontology.relational.Relation;
import net.sf.jtmt.ontology.relational.transactions.Transactions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Class with methods to write a journal of transactions. The journal() method
 * is called inline from the Prevayler transactions. The journal entries can 
 * be manually inspected before being uploaded to the master RDBMS store.
 * @author Sujit Pal
 * @version $Revision$
 */
public class DbJournaller extends JdbcDaoSupport {

  private static final Log log = LogFactory.getLog(DbJournaller.class);
  
  private static JdbcTemplate jdbcTemplate;
  
  static {
    DataSource dataSource = new DriverManagerDataSource(
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/ontodb", "root", "");
    jdbcTemplate = new JdbcTemplate(dataSource);
  }
  
  public static boolean journal(Transactions transaction, long userId, Date executionTime, Object... objs) {
    try {
      switch (transaction) {
        case addEntity: {
          Entity entity = (Entity) objs[0];
          addEntity(userId, executionTime, entity);
          break;
        }
        case updEntity: {
          Entity entity = (Entity) objs[0];
          updateEntity(userId, executionTime, entity);
          break;
        }
        case delEntity: {
          Entity entity = (Entity) objs[0];
          deleteEntity(userId, executionTime, entity);
          break;
        }
        case addAttr: {
          Entity entity = (Entity) objs[0];
          Attribute attribute = (Attribute) objs[1];
          addAttribute(userId, executionTime, entity, attribute);
          break;
        }
        case updAttr: {
          Entity entity = (Entity) objs[0];
          Attribute attribute = (Attribute) objs[1];
          updateAttribute(userId, executionTime, entity, attribute);
          break;
        }
        case delAttr: {
          Entity entity = (Entity) objs[0];
          Attribute attribute = (Attribute) objs[1];
          deleteAttribute(userId, executionTime, entity, attribute);
          break;
        }
        case addRel: {
          Relation relation = (Relation) objs[0];
          addRelation(userId, executionTime, relation);
          break;
        }
        case addFact: { 
          Fact fact = (Fact) objs[0];
          addFact(userId, executionTime, fact);
          break;
        }
        case delFact: { 
          Fact fact = (Fact) objs[0];
          removeFact(userId, executionTime, fact);
        }
        default:
          break;
      }
      return true;
    } catch (Exception e) {
      log.error(e);
      return false;
    }
  }
  
  private static void addEntity(long userId, Date executionTime, Entity entity) {
    if (isTransactionApplied(executionTime, userId, Transactions.addEntity)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", entity.getId());
    jsonObj.put("name", entity.getName());
    insertJournal(userId, Transactions.addEntity, jsonObj);
  }
  
  private static void updateEntity(long userId, Date executionTime, Entity entity) {
    if (isTransactionApplied(executionTime, userId, Transactions.updEntity)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", entity.getId());
    jsonObj.put("name", entity.getName());
    insertJournal(userId, Transactions.updEntity, jsonObj);
  }
  
  private static void deleteEntity(long userId, Date executionTime, Entity entity) {
    if (isTransactionApplied(executionTime, userId, Transactions.delEntity)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", entity.getId());
    jsonObj.put("name", entity.getName());
    insertJournal(userId, Transactions.delEntity, jsonObj);
  }

  private static void addAttribute(long userId, Date executionTime, Entity entity, Attribute attribute) {
    if (isTransactionApplied(executionTime, userId, Transactions.addAttr)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("entityId", entity.getId());
    jsonObj.put("attributeName", attribute.getName());
    jsonObj.put("attributeValue", attribute.getValue());
    insertJournal(userId, Transactions.addAttr, jsonObj);
  }

  private static void updateAttribute(long userId, Date executionTime, Entity entity, Attribute attribute) {
    if (isTransactionApplied(executionTime, userId, Transactions.updAttr)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("entityId", entity.getId());
    jsonObj.put("attributeName", attribute.getName());
    jsonObj.put("attributeValue", attribute.getValue());
    insertJournal(userId, Transactions.updAttr, jsonObj);
  }

  private static void deleteAttribute(long userId, Date executionTime, Entity entity, Attribute attribute) {
    if (isTransactionApplied(executionTime, userId, Transactions.delAttr)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("entityId", entity.getId());
    jsonObj.put("attributeName", attribute.getName());
    jsonObj.put("attributeValue", attribute.getValue());
    insertJournal(userId, Transactions.delAttr, jsonObj);
  }

  private static void addRelation(long userId, Date executionTime, Relation relation) {
    if (isTransactionApplied(executionTime, userId, Transactions.addRel)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("relationId", relation.getId());
    jsonObj.put("relationName", relation.getName());
    insertJournal(userId, Transactions.addRel, jsonObj);
  }

  private static void addFact(long userId, Date executionTime, Fact fact) {
    if (isTransactionApplied(executionTime, userId, Transactions.addFact)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("sourceEntityId", fact.getSourceEntityId());
    jsonObj.put("targetEntityId", fact.getTargetEntityId());
    jsonObj.put("relationId", fact.getRelationId());
    insertJournal(userId, Transactions.addFact, jsonObj);
  }

  private static void removeFact(long userId, Date executionTime, Fact fact) {
    if (isTransactionApplied(executionTime, userId, Transactions.delFact)) {
      return;
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("sourceEntityId", fact.getSourceEntityId());
    jsonObj.put("targetEntityId", fact.getTargetEntityId());
    jsonObj.put("relationId", fact.getRelationId());
    insertJournal(userId, Transactions.delFact, jsonObj);
  }

  private static boolean isTransactionApplied(Date executionTime, long userId, Transactions tx) {
    int count = jdbcTemplate.queryForInt(
      "select count(*) from journal where log_date = ? and user_id = ? and tx_id = ?",
      new Object[] {executionTime, userId, tx.id()});
    return (count > 0);
  }

  private static void insertJournal(long userId, Transactions tx, JSONObject json) {
    jdbcTemplate.update(
      "insert into journal(user_id, tx_id, args) values (?, ?, ?)", 
      new Object[] {userId, tx.id(), json.toString()});
  }

}
