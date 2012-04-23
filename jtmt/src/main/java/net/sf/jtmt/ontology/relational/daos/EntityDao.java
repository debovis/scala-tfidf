package net.sf.jtmt.ontology.relational.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jtmt.ontology.relational.Attribute;
import net.sf.jtmt.ontology.relational.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Provides methods to save and load Entity objects from database.
 * @author Sujit Pal
 * @version $Revision$
 */
public class EntityDao extends JdbcDaoSupport {

  private final Log log = LogFactory.getLog(getClass());

  @SuppressWarnings("unchecked")
  public List<Entity> getAllEntities() {
    List<Entity> entities = new ArrayList<Entity>();
    List<Map<String,Object>> rows = getJdbcTemplate().queryForList(
      "select id, name from entities");
    for (Map<String,Object> row : rows) {
      Entity entity = new Entity();
      entity.setId((Integer) row.get("ID"));
      entity.setName((String) row.get("NAME"));
      entities.add(entity);
    }
    return entities;
  }

  @SuppressWarnings("unchecked")
  public Entity getById(long id) {
    try {
      Entity entity = new Entity();
      Map<String,Object> row = getJdbcTemplate().queryForMap(
        "select id, name from entities where id = ?", 
        new Long[] {id});
      entity.setId((Integer) row.get("ID"));
      entity.setName((String) row.get("NAME"));
      return entity;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public Entity getByName(String name) {
    try {
      Entity entity = new Entity();
      Map<String,Object> row = getJdbcTemplate().queryForMap(
        "select id, name from entities where name = ?", 
        new String[] {name});
      entity.setId((Integer) row.get("ID"));
      entity.setName((String) row.get("NAME"));
      entity.setAttributes(getAttributes(entity.getId()));
      return entity;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<Attribute> getAttributes(long entityId) {
    List<Attribute> attributes = new ArrayList<Attribute>();
    List<Map<String,String>> rows = getJdbcTemplate().queryForList(
      "select at.attr_name, a.value " +
      "from attributes a, attribute_types at " +
      "where a.attr_id = at.id " +
      "and a.entity_id = ?", new Long[] {entityId});
    for (Map<String,String> row : rows) {
      String name = row.get("ATTR_NAME");
      String value = row.get("VALUE");
      Attribute attribute = new Attribute(name, value);
      attributes.add(attribute);
    }
    return attributes;
  }

  @SuppressWarnings("unchecked")
  public Attribute getAttributeByName(long entityId, String attributeName) {
    try {
      Attribute attribute = new Attribute();
      Map<String,String> row = getJdbcTemplate().queryForMap(
        "select at.attr_name, a.value " +
        "from attributes a, attribute_types at " +
        "where a.attr_id = at.id " +
        "and a.entity_id = ? " +
        "and at.attr_name = ?", new Object[] {entityId, attributeName});
      attribute.setName(row.get("NAME"));
      attribute.setValue(row.get("VALUE"));
      return attribute;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }

  public long getAttributeTypeId(final String attributeName) {
    try {
      long attributeTypeId = getJdbcTemplate().queryForLong(
        "select id from attribute_types where attr_name = ?", 
        new String[] {attributeName});
      return attributeTypeId;
    } catch (IncorrectResultSizeDataAccessException e) {
      return 0L;
    }
  }
  
  public long save(final Entity entity) {
    Entity dbEntity = getByName(entity.getName());
    if (dbEntity == null) {
      log.debug("Saving entity:" + entity.getName());
      // insert the entity
      KeyHolder entityKeyHolder = new GeneratedKeyHolder();
      getJdbcTemplate().update(new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn)
        throws SQLException {
          PreparedStatement ps = conn.prepareStatement(
            "insert into entities(name) values (?)", 
            Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, entity.getName());
          return ps;
        }
      }, entityKeyHolder);
      long entityId = entityKeyHolder.getKey().longValue();
      List<Attribute> attributes = entity.getAttributes();
      for (Attribute attribute : attributes) {
        saveAttribute(entityId, attribute);
      }
      // finally, always save the "english name" of the entity as an attribute
      saveAttribute(entityId, new Attribute("EnglishName", getEnglishName(entity)));
      return entityId;
    } else {
      getJdbcTemplate().update("update entities set name = ? where id = ?", 
        new Object[] {entity.getName(), entity.getId()});
      return entity.getId();
    }
  }

  public long saveAttribute(final long entityId, final Attribute attribute) {
    // check to see if attribute exists in attribute_types
    long attributeTypeId = getAttributeTypeId(attribute.getName());
    if (attributeTypeId == 0L) {
      attributeTypeId = saveAttributeType(attribute.getName());
    }
    Attribute dbAttribute = getAttributeByName(entityId, attribute.getName());
    final long attrId = attributeTypeId;
    if (dbAttribute == null) {
      KeyHolder keyholder = new GeneratedKeyHolder();
      final String attributeName = attribute.getName();
      getJdbcTemplate().update(new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn)
        throws SQLException {
          PreparedStatement ps = conn.prepareStatement(
            "insert into attributes(entity_id, attr_id, value) values (?, ?, ?)");
          ps.setLong(1, entityId);
          ps.setLong(2, attrId);
          ps.setString(3, attribute.getValue());
          return ps;
        }
      }, keyholder);
      long attributeId = keyholder.getKey().longValue();
      return attributeId;
    } else {
      getJdbcTemplate().update(
        "update attributes set value = ? where entity_id = ? and attr_id = ?", 
        new Long[] {entityId, attrId});
      return attrId;
    }
  }

  public long saveAttributeType(final String attributeName) {
    long attributeTypeId = getAttributeTypeId(attributeName);
    if (attributeTypeId == 0L) {
      KeyHolder keyholder = new GeneratedKeyHolder();
      getJdbcTemplate().update(new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn)
        throws SQLException {
          PreparedStatement ps = conn.prepareStatement(
            "insert into attribute_types(attr_name) values (?)");
          ps.setString(1, attributeName);
          return ps;
        }
      }, keyholder);
      attributeTypeId = keyholder.getKey().longValue();
    }
    return attributeTypeId;
  }
    
  /**
   * Split up Uppercase Camelcased names (like Java classnames or C++ variable
   * names) into English phrases by splitting wherever there is a transition 
   * from lowercase to uppercase.
   * @param name the input camel cased name.
   * @return the "english" name.
   */
  public String getEnglishName(Entity entity) {
    if (entity == null) {
      return null;
    }
    StringBuilder englishNameBuilder = new StringBuilder();
    char[] namechars = entity.getName().toCharArray();
    for (int i = 0; i < namechars.length; i++) {
      if (i > 0 && Character.isUpperCase(namechars[i]) && 
          Character.isLowerCase(namechars[i-1])) {
        englishNameBuilder.append(' ');
      }
      englishNameBuilder.append(namechars[i]);
    }
    return englishNameBuilder.toString();
  }
}
