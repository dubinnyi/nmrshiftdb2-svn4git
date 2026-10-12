/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012,2014 Stefan Kuhn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscience.nmrshiftdb.om;

// JDK classes
import java.sql.Connection;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    May 6, 2003
 */
public class DBBookPeer
     extends org.openscience.nmrshiftdb.om.BaseDBBookPeer {
  /**
   *  Gets a list of titles and subtitles of existing monographs (i. e. books containing no artice).
   *
   * @return                The titles.
   * @exception  Exception  Database problems.
   */
  public static List<Record> getExistingBookTitles() throws Exception {
    String sql = "select distinct TITLE, SUBTITLE from LITERATURE, BOOK left join ARTICLE on BOOK.LITERATURE_ID=ARTICLE.LITERATURE_ID where ARTICLE.LITERATURE_ID is null and LITERATURE.LITERATURE_ID=BOOK.LITERATURE_ID;";
    return (executeQuery(sql));
  }
  
  
  public static DBLiterature getExistingOrMakeNewOne(String authorbook, String titlebook, String subtitlebook, boolean etalbook, Connection dbconn, String place, String publisher, int year) throws TorqueException, DataSetException{
    List<StringAndStringAndBoolean> list = DBLiterature.analyzeAuthor(authorbook);
    StringBuffer sql = new StringBuffer("select LITERATURE.LITERATURE_ID from LITERATURE, ");
    for (int i = 0; i < list.size(); i++) {
      sql.append("IS_EDITOR as i" + i + ", AUTHOR as a" + i + ", ");
    }
    sql.append("BOOK, BOOK_PUBLISHER, PUBLISHER where LITERATURE.TITLE=\"" + titlebook + "\" and LITERATURE.SUBTITLE =\"" + subtitlebook + "\" and LITERATURE.ET_AL =\"" + (etalbook ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE) + "\" and ");
    for (int i = 0; i < list.size(); i++) {
      sql.append("LITERATURE.LITERATURE_ID =i" + i + ".LITERATURE_ID and i" + i + ".AUTHOR_ID =a" + i + ".AUTHOR_ID and ");
    }
    for (int i = 0; i < list.size(); i++) {
      StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(i);
      sql.append("a" + i + ".NAME =\"" + ssb.string1 + "\" and a" + i + ".SURNAME =\"" + ssb.string2 + "\" and ");
    }
    sql.append("LITERATURE.LITERATURE_ID=BOOK.LITERATURE_ID and BOOK.LITERATURE_ID=BOOK_PUBLISHER.LITERATURE_ID and BOOK_PUBLISHER.PUBLISHER_ID=PUBLISHER.PUBLISHER_ID and BOOK_PUBLISHER.YEAR=\"" + year + "\" and ");
    sql.append("PUBLISHER.PLACE=\"" + place + "\" and PUBLISHER.NAME=\"" + publisher + "\";");
    List<Record> l = DBLiteraturePeer.executeQuery(sql.toString());
    if (l.size() > 0) {
      return (DBLiteraturePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt())));
    }
    DBLiterature booklit=new DBLiterature();
    booklit.setTitle(titlebook);
    booklit.setSubtitle(subtitlebook);
    booklit.setTitleTotalSoundex(Soundex.soundex(titlebook + subtitlebook, "9", new StringAndInt()));
    booklit.setTitleTotal(titlebook + subtitlebook);
    if (etalbook) {
      booklit.setEtAl(NmrshiftdbConstants.TRUE);
    }
    booklit.save(dbconn);
    list = DBLiterature.analyzeAuthor(authorbook);
    for (int k = 0; k < list.size(); k++) {
      StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(k);
      DBAuthor dbauthor = DBAuthorPeer.getOldOrMakeNew(ssb.string1, ssb.string2,dbconn);
      IsEditor litaut = new IsEditor();
      litaut.setDBAuthor(dbauthor);
      litaut.setDBLiterature(booklit);
      litaut.setEditorOrder(k);
      litaut.save(dbconn);
    }
    DBBook book = new DBBook();
    book.setDBLiterature(booklit);
    book.save(dbconn);
    DBPublisher dbpub = null;
    dbpub = DBPublisherPeer.getOldOrMakeNew(place, publisher,dbconn);
    DBBookDBPublisher bookpub = new DBBookDBPublisher();
    bookpub.setDBPublisher(dbpub);
    bookpub.setDBBook(book);
    bookpub.setYear(year);
    bookpub.save(dbconn);
    return(booklit);
  }
}

