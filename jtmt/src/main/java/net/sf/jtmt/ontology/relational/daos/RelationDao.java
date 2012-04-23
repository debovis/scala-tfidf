package net.sf.jtmt.ontology.relational.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jtmt.ontology.relational.Relation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Provides methods to save and load Relation objects from database.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RelationDao extends JdbcDaoSupport {

  private final Log log = LogFactory.getLog(getClass());
  
  @SuppressWarnings("unchecked")
  public List<Relation> getAllRelations() {
    List<Relation> relations = new ArrayList<Relation>();
    List<Map<String,Object>> rows = getJdbcTemplate().queryForList(
      "select id, name from relations where id > 0");
    for (Map<String,Object> row : rows) {
      Relation relation = new Relation();
      relation.setRelationId((Integer) row.get("ID"));
      relation.setRelationName((String) row.get("NAME"));
      relations.add(relation);
    }
    return relations;
  }

  @SuppressWarnings("unchecked")
  public Relation getById(long relationId) {
    Relation relation = new Relation();
    try {
      Map<String,Object> row = getJdbcTemplate().queryForMap(
        "select id, name from relations where id = ?", 
        new Long[] {relationId});
      relation.setRelationId((Integer) row.get("ID"));
      relation.setRelationName((String) row.get("NAME"));
      return relation;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public Relation getByName(String name) {
    Relation relation = new Relation();
    try {
      Map<String,Object> row = getJdbcTemplate().queryForMap(
        "select id, name from relations where id = ?", 
        new String[] {name});
      relation.setRelationId((Integer) row.get("ID"));
      relation.setRelationName((String) row.get("NAME"));
      return relation;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }
  
  public boolean isBidirectional(long relationId) {
    int count = getJdbcTemplate().queryForInt(
      "select count(*) from relations where id = ?", 
      new Long[] {-1L * relationId});
    return count > 0;
  }
  
  public long save(final Relation relation) {
    Relation dbRelation = getByName(relation.getName());
    if (dbRelation == null) {
      KeyHolder keyholder = new GeneratedKeyHolder();
      getJdbcTemplate().update(new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn) 
            throws SQLException {
          PreparedStatement ps = conn.prepareStatement(
            "insert into relations(name) values (?)", 
            Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, relation.getName());
          return ps;
        }
      }, keyholder);
      return keyholder.getKey().longValue();
    } else {
      getJdbcTemplate().update("update relations set name = ? where id = ?",
        new Object[] {relation.getName(), relation.getId()});
      return relation.getId();
    }
  }
}
