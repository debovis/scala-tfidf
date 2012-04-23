// $Id$
package net.sf.jtmt.phrase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.jtmt.phrase.db.DBWritable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.hadoop.io.Writable;

/**
 * A writable that can be used to read and write data from the table
 * tmdb:my_phrase_counts:
 * +-----------+--------------+------+-----+---------+-------+
 * | Field     | Type         | Null | Key | Default | Extra |
 * +-----------+--------------+------+-----+---------+-------+
 * | phrase    | varchar(255) | NO   | MUL | NULL    |       | 
 * | gram_size | int(11)      | NO   | MUL | NULL    |       | 
 * | occurs    | mediumtext   | NO   |     | NULL    |       | 
 * +-----------+--------------+------+-----+---------+-------+
 * @author Sujit Pal
 * @version $Revision$
 */
public class PhraseCounterDbWritable implements Writable, DBWritable {

  private String phrase;
  private int gramSize;
  private long occurs;
  
  public String getPhrase() {
    return phrase;
  }

  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  public int getGramSize() {
    return gramSize;
  }

  public void setGramSize(int gramSize) {
    this.gramSize = gramSize;
  }

  public long getOccurs() {
    return occurs;
  }

  public void setOccurs(long occurs) {
    this.occurs = occurs;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    phrase = in.readUTF();
    gramSize = in.readInt();
    occurs = in.readLong();
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeUTF(phrase);
    out.writeInt(gramSize);
    out.writeLong(occurs);
  }

  @Override
  public void readFields(ResultSet resultSet) throws SQLException {
    phrase = resultSet.getString(1);
    gramSize = resultSet.getInt(2);
    occurs = resultSet.getLong(3);
  }

  @Override
  public void write(PreparedStatement statement) throws SQLException {
    statement.setString(1, phrase);
    statement.setInt(2, gramSize);
    statement.setLong(3, occurs);
  }
  
  @Override
  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
  }
}
