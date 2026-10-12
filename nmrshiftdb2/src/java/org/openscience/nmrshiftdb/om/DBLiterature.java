/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012-2014 Stefan Kuhn
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import nu.xom.Element;
import nu.xom.Text;

import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 2, 2003
 */
public class DBLiterature
     extends org.openscience.nmrshiftdb.om.BaseDBLiterature
     implements Persistent {
  /**
   *  Type of literature as in NmrshiftdbConstants
   */
  public String type = "";
  /**
   *  The authors like s. a; b. c
   */
  private String author = "";
  /**
   *  More authors than stated
   */
  public boolean etalprov = false;
  /**
   *  In case it's an article in a book more authors than stated
   */
  public boolean etalbook = false;
  /**
   *  In case it's an article the page it starts at
   */
  public int from;
  /**
   *  In case it's an article the page it end at
   */
  public int to;
  /**
   *  In case it's a journal article: The title of the journal
   */
  public String journaltitle = "";
  /**
   *  In case it's a journal article: The subtitle of the journal
   */
  public String journalsubtitle = "";
  /**
   *  In case it's a journal article: The id of a journal already in DB
   */
  public int choosenJournal = 0;
  /**
   *  In case it's a journal article: The title abbreviation
   */
  public String ita = "";
  /**
   *  In case it's a journal article: The volume
   */
  public int volume;
  /**
   *  In case it's a journal article: The year
   */
  public int year;
  /**
   *  In case it's a journal article: The issue
   */
  public int issue;
  /**
   *  In case it's a book: The publishing place
   */
  public String place = "";
  /**
   *  In case it's a book: The publisher name
   */
  public String publisher = "";
  /**
   *  In case it's an article in a book: The author like a. b; c. d
   */
  private String authorbook = "";
  private String editorbook = "";
  /**
   *  In case it's an article in a book: The title
   */
  private String titlebook = "";
  /**
   *  In case it's an article in a book: The subtitle
   */
  private String subtitlebook = "";


  /**
   *Constructor for the DBLiterature object
   */
  public DBLiterature() {
    this.setTitle("");
    this.setSubtitle("");
    this.setDoi("");
    this.setUrl("");
  }

  /**
   *Constructor for the DBLiterature object.
   *
   * @param  type  The type of Literature.
   */
  public DBLiterature(String type) {
    this();
    this.type = type;
  }

  /**
   * Builds a DBLiterature from a bibtex entry (reverse of getAsBibtex).
   * 
   * @param bibtex The bibtex entry
   */
  public DBLiterature(Element bibtex){
	  	Element literatureentry=null;
		if(bibtex.getChildElements("book","http://bibtexml.sf.net/").size()>0){
			this.type=NmrshiftdbConstants.MONOGRAPH;
			this.place=bibtex.getChildElements("book","http://bibtexml.sf.net/").get(0).getChildElements("place","http://bibtexml.sf.net/").get(0).getValue();
			this.publisher=bibtex.getChildElements("book","http://bibtexml.sf.net/").get(0).getChildElements("publisher","http://bibtexml.sf.net/").get(0).getValue();
			literatureentry=bibtex.getChildElements("book","http://bibtexml.sf.net/").get(0);
		}else if(bibtex.getChildElements("article","http://bibtexml.sf.net/").size()>0){
			this.type=NmrshiftdbConstants.JOURNAL_ARTICLE;
			String journalentry=bibtex.getChildElements("article","http://bibtexml.sf.net/").get(0).getChildElements("journal","http://bibtexml.sf.net/").get(0).getValue();
			if(journalentry.indexOf("(")>-1){
			    if(journalentry.indexOf(")")>-1)
			        this.ita=journalentry.substring(journalentry.indexOf("(")+1,journalentry.indexOf(")"));
			    else 
			        this.ita=journalentry.substring(journalentry.indexOf("(")+1);
				journalentry=journalentry.substring(0,journalentry.indexOf("("));
			}
			if(journalentry.indexOf(".")>-1){
				this.journalsubtitle=journalentry.substring(journalentry.indexOf(".")+1);
				journalentry=journalentry.substring(0,journalentry.indexOf(".")-1);
			}else{
				this.journalsubtitle="";
			}
			this.journaltitle=journalentry;
			this.volume=Integer.parseInt(bibtex.getChildElements("article","http://bibtexml.sf.net/").get(0).getChildElements("volume","http://bibtexml.sf.net/").get(0).getValue());
			if(bibtex.getChildElements("article","http://bibtexml.sf.net/").get(0).getChildElements("pages","http://bibtexml.sf.net/").size()>0){
				String pages=bibtex.getChildElements("article","http://bibtexml.sf.net/").get(0).getChildElements("pages","http://bibtexml.sf.net/").get(0).getValue();
				this.from=Integer.parseInt(pages.substring(0,pages.indexOf("-")));
				if(pages.indexOf("?")==-1)
					this.to=Integer.parseInt(pages.substring(pages.indexOf("-")+1));
			}
			literatureentry=bibtex.getChildElements("article","http://bibtexml.sf.net/").get(0);
		}else if(bibtex.getChildElements("inbook","http://bibtexml.sf.net/").size()>0){
			if(bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("editor","http://bibtexml.sf.net/").size()>0){
				this.setAuthorbook(bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("editor","http://bibtexml.sf.net/").get(0).getValue());
			}
			this.place=bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("place","http://bibtexml.sf.net/").get(0).getValue();
			this.publisher=bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("publisher","http://bibtexml.sf.net/").get(0).getValue();
			if(bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("pages","http://bibtexml.sf.net/").size()>0){
    			String pages=bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("pages","http://bibtexml.sf.net/").get(0).getValue();
    			this.from=Integer.parseInt(pages.substring(0,pages.indexOf("-")));
    			if(pages.indexOf("?")==-1)
    				this.to=Integer.parseInt(pages.substring(pages.indexOf("-")+1));
			}
			this.titlebook=bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0).getChildElements("booktitle","http://bibtexml.sf.net/").get(0).getValue();
			literatureentry=bibtex.getChildElements("inbook","http://bibtexml.sf.net/").get(0);
		}
		this.setAuthor(literatureentry.getChildElements("author","http://bibtexml.sf.net/").get(0).getValue());
		this.etalprov=literatureentry.getChildElements("etal","http://bibtexml.sf.net/").get(0).getValue().equals(NmrshiftdbConstants.TRUE);
		if(literatureentry.getChildElements("title","http://bibtexml.sf.net/").size()>0){
			String title=literatureentry.getChildElements("title","http://bibtexml.sf.net/").get(0).getValue();
			if(title.indexOf(".")>0){
				this.setSubtitle(title.substring(title.indexOf(".")+1));
				title=title.substring(0,title.indexOf("."));
			}else{
				this.setSubtitle("");
			}
			this.setTitle(title);
			this.setTitleTotalSoundex(Soundex.soundex(title + this.getSubtitle(), "9", new StringAndInt()));
			this.setTitleTotal(title + this.getSubtitle());
		}else{
			this.setTitle("");
			this.setSubtitle("");
			this.setTitleTotal("");
			this.setTitleTotalSoundex("");
		}		
		this.year=Integer.parseInt(literatureentry.getChildElements("year","http://bibtexml.sf.net/").get(0).getValue());
		if(literatureentry.getChildElements("doi","http://bibtexml.sf.net/").size()>0){
			this.setDoi(literatureentry.getChildElements("doi","http://bibtexml.sf.net/").get(0).getValue());
		}else{
			this.setDoi("");
		}
		if(literatureentry.getChildElements("url","http://bibtexml.sf.net/").size()>0){
			this.setUrl(literatureentry.getChildElements("url","http://bibtexml.sf.net/").get(0).getValue());
		}else{
			this.setUrl("");
		}
  }

  /**
   *  Gets the type attribute of the DBLiterature object.
   *
   * @return    The type value
   */
  public String getType() {
    return (type);
  }


  /**
   *  Gets the authorbook attribute of the DBLiterature object
   *
   * @return    The authorbook value
   */
  public String getAuthorBook() {
    return (authorbook);
  }


  /**
   *  Gets the editorbook attribute of the DBLiterature object
   *
   * @return    The author value
   */
  public String getEditorBook() {
    return (editorbook);
  }
  
  /**
   *  Gets the author attribute of the DBLiterature object
   *
   * @return    The author value
   */
  public String getAuthor() {
    return (author);
  }


  /**
   *  Gets the etalprov attribute of the DBLiterature object
   *
   * @return    The etalprov value
   */
  public boolean getEtalprov() {
    return (etalprov);
  }


  /**
   *  Gets the etalbook attribute of the DBLiterature object
   *
   * @return    The etalbook value
   */
  public boolean getEtalbook() {
    return (etalbook);
  }


  /**
   *  Gets the journalsubtitle attribute of the DBLiterature object
   *
   * @return    The journalsubtitle value
   */
  public String getJournalsubtitle() {
    return (journalsubtitle);
  }


  /**
   *  Gets the journaltitle attribute of the DBLiterature object
   *
   * @return    The journaltitle value
   */
  public String getJournaltitle() {
    return (journaltitle);
  }


  /**
   *  Gets the subtitlebook attribute of the DBLiterature object
   *
   * @return    The subtitlebook value
   */
  public String getSubtitlebook() {
    return (subtitlebook);
  }


  /**
   *  Gets the ita attribute of the DBLiterature object
   *
   * @return    The ita value
   */
  public String getIta() {
    return (ita);
  }


  /**
   *  Gets the titlebook attribute of the DBLiterature object
   *
   * @return    The titlebook value
   */
  public String getTitlebook() {
    return (titlebook);
  }


  /**
   *  Gets the title with " esacped for html
   *
   * @return    The titleForHtml value
   */
  public String getTitleForHtml() {
    return (GeneralUtils.replace(getTitle(), "\"", "&quot;"));
  }


  /**
   *  Gets the subtitle with " escaped for html.
   *
   * @return    The subtitleForHtml value.
   */
  public String getSubtitleForHtml() {
    return (GeneralUtils.replace(getSubtitle(), "\"", "&quot;"));
  }


  /**
   *  Gets the titlebook with " escaped for html.
   *
   * @return    The titlebookForHtml value.
   */
  public String getTitlebookForHtml() {
    return (GeneralUtils.replace(getTitlebook(), "\"", "&quot;"));
  }


  /**
   *  Gets the subtitlebook with " escaped for html.
   *
   * @return    The subtitlebookForHtml value.
   */
  public String getSubtitlebookForHtml() {
    return (GeneralUtils.replace(getSubtitlebook(), "\"", "&quot;"));
  }



  /**
   *  Gets the place attribute of the DBLiterature object
   *
   * @return    The place value
   */
  public String getPlace() {
    return (place);
  }


  /**
   *  Gets the publisher attribute of the DBLiterature object
   *
   * @return    The publisher value
   */
  public String getPublisher() {
    return (publisher);
  }


  /**
   *  Gets the year attribute of the DBLiterature object
   *
   * @return    The year value
   */
  public int getYear() {
    return (year);
  }


  /**
   *  Gets the volume attribute of the DBLiterature object
   *
   * @return    The volume value
   */
  public int getVolume() {
    return (volume);
  }


  /**
   *  Gets the issue attribute of the DBLiterature object
   *
   * @return    The issue value
   */
  public int getIssue() {
    return (issue);
  }


  /**
   *  Gets the from attribute of the DBLiterature object
   *
   * @return    The from value
   */
  public int getFrom() {
    return (from);
  }


  /**
   *  Gets the to attribute of the DBLiterature object
   *
   * @return    The to value
   */
  public int getTo() {
    return (to);
  }


  /**
   *  Gets the authorbook attribute of the DBLiterature object
   *
   * @return    The authorbook value
   */
  public String getAuthorbook() {
    return (authorbook);
  }


  /**
   *  Gets the authors of this DBLiterature object.
   *
   * @return                          The authors value.
   * @exception  NmrshiftdbException  Unique problems.
   * @exception  TorqueException            Database problems.
   */
  public List<DBAuthor> getAuthors() throws NmrshiftdbException, TorqueException {
    Criteria crit = new Criteria();
    crit.add(IsAuthorPeer.LITERATURE_ID, this.getLiteratureId());
    crit.addAscendingOrderByColumn(IsAuthorPeer.AUTHOR_ORDER);
    crit.addJoin(IsAuthorPeer.AUTHOR_ID, DBAuthorPeer.AUTHOR_ID);
    List<DBAuthor> v = DBAuthorPeer.doSelect(crit);
    return (v);
  }
  
  /**
   *  Gets the editors of thise DBLiterature object.
   *
   * @return                          The editors value.
   * @exception  NmrshiftdbException  Unique problems.
   * @exception  TorqueException            Database problems.
   */
  public List<DBAuthor> getEditors() throws NmrshiftdbException, TorqueException {
    Criteria crit = new Criteria();
    crit.add(IsEditorPeer.LITERATURE_ID, this.getLiteratureId());
    crit.addAscendingOrderByColumn(IsEditorPeer.EDITOR_ORDER);
    crit.addJoin(IsEditorPeer.AUTHOR_ID, DBAuthorPeer.AUTHOR_ID);
    List<DBAuthor> v2 = DBAuthorPeer.doSelect(crit);
    return (v2);
  }

  /**
   *  Tells which book this item is published in.
   *
   * @return                          The publishedInBook value, null=no book.
   * @exception  NmrshiftdbException  Unique problems
   * @exception  TorqueException            Database problems.
   */
  public DBLiterature getPublishedInBook() throws NmrshiftdbException, TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBArticlePeer.LITERATURE_ID, this.getLiteratureId());
    List<DBArticle> v = DBArticlePeer.doSelect(crit);
    if (v.size() == 0 || ((DBArticle) v.get(0)).getPublishedInBook() == 0) {
      return (null);
    } else {
      return (DBLiteraturePeer.retrieveByPK(((DBArticle) v.get(0)).getPublishedInBook()));
    }
  }


  /**
   *  Returns this literature item as an article, in case it is one.
   *
   * @return                The article value, null=no article.
   * @exception  TorqueException  Database problems.
   */
  public DBArticle getArticle() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBArticlePeer.LITERATURE_ID, this.getLiteratureId());
    List<DBArticle> v = DBArticlePeer.doSelect(crit);
    if (v.size() == 0) {
      return (null);
    } else {
      return ((DBArticle) v.get(0));
    }
  }
  
  
  /**  Tells if this a monograph or not.
   *
   * @return                          True or false.
   * @exception  NmrshiftdbException  Problems with unique columns.
   * @exception  TorqueException            Database problems.
   */
  public boolean isMonograph() throws NmrshiftdbException, TorqueException {
    if (getArticle() == null) {
      return (true);
    } else {
      return (false);
    }
  }


  /**
   *  Tells if this literature item is published in a journal.
   *
   * @return                True or false.
   * @exception  TorqueException  Database problems.
   */
  public boolean isPublishedInJournal() throws TorqueException {
    if (getArticle() != null && getArticle().getPublishedInJournal()!=0) {
      return (true);
    } else {
      return (false);
    }
  }


  /**
   *  Gets all authors of the DBLiterature object.
   *
   * @return                The authorsFromDB value.
   * @throws NmrshiftdbException 
   * @exception  TorqueException  Database problems.
   */
  public String getAuthorsFromDB() throws TorqueException, NmrshiftdbException {
    List<DBAuthor> v = getAuthors();
    StringBuffer authorlocal = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      authorlocal.append(((DBAuthor) v.get(i)).getSurname() + " " + ((DBAuthor) v.get(i)).getName() + "; ");
    }
    GeneralUtils.removeLastComma(authorlocal);
    return (authorlocal.toString());
  }
  
  /**
   *  Gets all editors of the DBLiterature object.
   *
   * @return                The authorsFromDB value.
   * @throws NmrshiftdbException 
   * @exception  TorqueException  Database problems.
   */
  public String getEditorsFromDB() throws TorqueException, NmrshiftdbException {
    List<DBAuthor> v = getEditors();
    StringBuffer authorlocal = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      authorlocal.append(((DBAuthor) v.get(i)).getSurname() + " " + ((DBAuthor) v.get(i)).getName() + "; ");
    }
    GeneralUtils.removeLastComma(authorlocal);
    return (authorlocal.toString());
  }


  /**
   *  Gets the place the DBLiterature object has been published.
   *
   * @return                The placeFromDB value.
   * @exception  TorqueException  Database problems.
   */
  public String getPlaceFromDB() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBookDBPublisherPeer.LITERATURE_ID, this.getLiteratureId());
    List<DBBookDBPublisher> v = DBBookDBPublisherPeer.doSelect(crit);
    if (v.size() == 0) {
      return (null);
    } else {
        return (DBPublisherPeer.retrieveByPK(((DBBookDBPublisher) v.get(0)).getPublisherId()).getPlace());
    }
  }


  /**
   *  Gets the name of the publisher the DBLiterature has been published by.
   *
   * @return                The publisherNameFromDB value.
   * @exception  TorqueException  Database problems.
   */
  public DBPublisher getPublisherFromDB() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBookDBPublisherPeer.LITERATURE_ID, this.getLiteratureId());
    List<DBBookDBPublisher> v = DBBookDBPublisherPeer.doSelect(crit);
    if (v.size() == 0) {
      return (null);
    } else {
        return (DBPublisherPeer.retrieveByPK(((DBBookDBPublisher) v.get(0)).getPublisherId()));
    }
  }
  
  
  /**
   *  Gets the name of the publisher the DBLiterature has been published by.
   *
   * @return                The publisherNameFromDB value.
   * @exception  TorqueException  Database problems.
   */
  public String getPublisherNameFromDB() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBookDBPublisherPeer.LITERATURE_ID, this.getLiteratureId());
    List<DBBookDBPublisher> v = DBBookDBPublisherPeer.doSelect(crit);
    if (v.size() == 0) {
      return (null);
    } else {
        return (DBPublisherPeer.retrieveByPK(((DBBookDBPublisher) v.get(0)).getPublisherId()).getName());
    }
  }


  /**
   *  Gets the publishing year the DBLiterature has been published in.
   *
   * @return                The publisherNameFromDB value.
   * @exception  TorqueException  Database problems.
   */
  public int getYearFromDB() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBookDBPublisherPeer.LITERATURE_ID, this.getLiteratureId());
    List<DBBookDBPublisher> v = DBBookDBPublisherPeer.doSelect(crit);
    if (v.size() == 0) {
      return (0);
    } else {
      return (((DBBookDBPublisher) v.get(0)).getYear());
    }
  }


  /**
   *  Saves this literature object to the db. The relevant flags need to be set.
   *
   * @param  spectrum       The spectrum this literature is of.
   * @param  dbconn         Description of Parameter
   * @exception  TorqueException  Database problems.
   */
  public void save(int spectrumid, Connection dbconn) throws TorqueException, DataSetException {
    DBLiterature oldLit = null;
    if ((oldLit = literatureAlreadyExists(dbconn)) != null) {
      if(spectrumid>-1){
          DBSpectrumDBLiterature litspec = new DBSpectrumDBLiterature();
          litspec.setSpectrumId(spectrumid);
          litspec.setDBLiterature(oldLit);
          litspec.save(dbconn);
      }
      this.setLiteratureId(oldLit.getLiteratureId());
    } else {
      boolean oldIsNew = isNew();
      if (isNew()) {
        if (etalprov) {
          setEtAl(NmrshiftdbConstants.TRUE);
        }
        if (!getUrl().equals("")) {
          if (getUrl().length() < 8 || (!getUrl().substring(0, 7).equals("http://") && !getUrl().substring(0, 8).equals("https://"))) {
            setUrl("http://" + getUrl());
          }
        }
        setTitleTotalSoundex(Soundex.soundex(getTitle() + getSubtitle(), "9", new StringAndInt()));
        setTitleTotal(getTitle() + getSubtitle());
      }
      super.save(dbconn);
      if (oldIsNew) {
        if(spectrumid>-1){
            DBSpectrumDBLiterature litspec = new DBSpectrumDBLiterature();
            litspec.setSpectrumId(spectrumid);
            litspec.setDBLiterature(this);
            litspec.save(dbconn);
        }
        List<StringAndStringAndBoolean> list = analyzeAuthor(author);
        for (int k = 0; k < list.size(); k++) {
          StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(k);
          DBAuthor dbauthor = DBAuthorPeer.getOldOrMakeNew(ssb.string1, ssb.string2,dbconn);
          IsAuthor litaut = new IsAuthor();
          litaut.setDBAuthor(dbauthor);
          litaut.setDBLiterature(this);
          litaut.setAuthorOrder(k);
          litaut.save(dbconn);
        }
        if (type.equals(NmrshiftdbConstants.MONOGRAPH)) {
          DBBook book = new DBBook();
          book.setDBLiterature(this);
          book.save(dbconn);
          DBPublisher dbpub = null;
          dbpub = DBPublisherPeer.getOldOrMakeNew(place, publisher,dbconn);
          DBBookDBPublisher bookpub = new DBBookDBPublisher();
          bookpub.setDBPublisher(dbpub);
          bookpub.setDBBook(book);
          bookpub.setYear(year);
          bookpub.save(dbconn);
          List<StringAndStringAndBoolean> editors = analyzeAuthor(editorbook);
          for (int k = 0; k < editors.size(); k++) {
            StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) editors.get(k);
            DBAuthor dbauthor = DBAuthorPeer.getOldOrMakeNew(ssb.string1, ssb.string2,dbconn);
            IsEditor litaut = new IsEditor();
            litaut.setDBAuthor(dbauthor);
            litaut.setDBLiterature(this);
            litaut.setEditorOrder(k);
            litaut.save(dbconn);
          }
        }
        if (type.equals(NmrshiftdbConstants.JOURNAL_ARTICLE)) {
          DBJournal journal = null;
          if (journaltitle.equals("")) {
            journal = DBJournalPeer.retrieveByPK(choosenJournal);
          } else {
            journal = DBJournalPeer.getOldOrMakeNew(journaltitle, journalsubtitle, ita, dbconn);
          }
          DBJournalVolume journalvolume = DBJournalVolumePeer.getOldOrMakeNew(journal, volume, year, dbconn);
          DBArticle article = new DBArticle();
          article.setPagesFrom(from);
          article.setPagesTo(to);
          article.setIssueNumber(issue);
          article.setPublishedInJournal(journalvolume.getJournalVolumeId());
          article.setLiteratureId(this.getLiteratureId());
          article.save(dbconn);
        }
        if (type.equals(NmrshiftdbConstants.OTHER_ARTICLE)) {
          DBLiterature booklit = DBBookPeer.getExistingOrMakeNewOne(authorbook, titlebook, subtitlebook, etalbook, dbconn, place, publisher, year);
          DBArticle article = new DBArticle();
          article.setPagesFrom(from);
          article.setPagesTo(to);
          article.setLiteratureId(this.getLiteratureId());
          article.setPublishedInBook(booklit.getLiteratureId());
          article.save(dbconn);
          List<StringAndStringAndBoolean> editors = analyzeAuthor(editorbook);
          for (int k = 0; k < editors.size(); k++) {
            StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) editors.get(k);
            DBAuthor dbauthor = DBAuthorPeer.getOldOrMakeNew(ssb.string1, ssb.string2,dbconn);
            IsEditor litaut = new IsEditor();
            litaut.setDBAuthor(dbauthor);
            litaut.setDBLiterature(this);
            litaut.setEditorOrder(k);
            litaut.save(dbconn);
          }
        }
      }
    }
  }


  /**
   *  Reads the attributes from db.
   *
   * @return                          This object itself.
   * @exception  NmrshiftdbException  Problems with unique columns.
   * @exception  TorqueException            Database problems.
   */
  public DBLiterature read() throws TorqueException, NmrshiftdbException {
    author = getAuthorsFromDB();
    if (isMonograph()) {
      type = NmrshiftdbConstants.MONOGRAPH;
      DBLiterature publishedInBook = this;
      place = publishedInBook.getPlaceFromDB();
      publisher = publishedInBook.getPublisherNameFromDB();
      year = publishedInBook.getYearFromDB();
      editorbook = publishedInBook.getEditorsFromDB();
    } else {
      if (isPublishedInJournal()) {
        type = NmrshiftdbConstants.JOURNAL_ARTICLE;
        DBArticle article = getArticle();
        from = article.getPagesFrom();
        to = article.getPagesTo();
        DBJournalVolume journalvolume = article.getDBJournalVolume();
        volume = journalvolume.getVolume();
        year = journalvolume.getYear();
        issue = article.getIssueNumber();
        DBJournal journal = journalvolume.getDBJournal();
        journaltitle = journal.getTitle();
        journalsubtitle = journal.getSubtitle();
      } else {
        type = NmrshiftdbConstants.OTHER_ARTICLE;
        DBLiterature publishedInBook = getPublishedInBook();
        if (publishedInBook.getEtAl().equals(NmrshiftdbConstants.TRUE)) {
          etalbook = true;
        }
        Criteria crit = new Criteria();
        crit.add(DBBookDBPublisherPeer.LITERATURE_ID, getPublishedInBook().getLiteratureId());
        List<DBBookDBPublisher> v = DBBookDBPublisherPeer.doSelect(crit);
        if (v.size() != 0) {
            year = ((DBBookDBPublisher) v.get(0)).getYear();
        }
        DBArticle article = getArticle();
        from = article.getPagesFrom();
        to = article.getPagesTo();
        place = publishedInBook.getPlaceFromDB();
        publisher = publishedInBook.getPublisherNameFromDB();
        authorbook = publishedInBook.getAuthorsFromDB();
        editorbook = publishedInBook.getEditorsFromDB();
        titlebook = publishedInBook.getTitle();
        subtitlebook = publishedInBook.getSubtitle();
      }
    }
    if (getEtAl().equals(NmrshiftdbConstants.TRUE)) {
      etalprov = true;
    }
    return (this);
  }


  /**
   *  Gives a nicly formatted string.
   *
   * @return    The citation of this literature.
   */
  public String toString() {
	  return toString(true);
  }
  
  /**
   *  Gives a nicly formatted string.
   *
   * @param withulr true=doi is url, false=doi is text
   * @return    The citation of this literature.
   */
  public String toString(boolean withurl) {
    try {
      if (isPublishedInJournalFromAttributes()) {
        return (author + (etalprov ? " et al." : "") + ": " + getTitle() + (getSubtitle().equals("") ? "" : ". " + getSubtitle()) + ", in: " + getJournalData(withurl) + ".");
      } else if (!titlebook.equals("")) {
        return (author + (etalprov ? " et al." : "") + ": " + getTitle() + (getSubtitle().equals("") ? "" : ". " + getSubtitle()) + ", in: " + authorbook + (etalbook ? " et al. (ed.)" : "") + ": " + titlebook + (subtitlebook.equals("") ? "" : ". " + subtitlebook) + ", " + (!editorbook.equals("") ? "ed. "+editorbook+", " : "") + place + ": " + publisher + " " + year + ", pp. " + from + " - " + to + (getDoi().equals("") ? "" : " (DOI: "+(withurl ? "<a href=\"derefer.jsp?url=http://dx.doi.org/"+getDoi()+"\">" + getDoi() + "</a>)" : getDoi()+")")) + (withurl && !getUrl().equals("") ? " (URL: <a href=\"" + getUrl() + "\">" + getUrl() + "</a>)" : "") + ".");
      } else {
        return (author + (etalprov ? " et al." : "") + ": " + getTitle() + (getSubtitle().equals("") ? "" : ". " + getSubtitle()) + ", " + (!editorbook.equals("") ? "ed. "+editorbook+", " : "") + place + ": " + publisher + " " + year + (getDoi().equals("") ? "" : " (DOI: "+(withurl ? "<a href=\"derefer.jsp?url=http://dx.doi.org/"+getDoi()+"\">" + getDoi() + "</a>)" : getDoi()+")")) + (withurl && !getUrl().equals("") ? " (URL: <a href=\"" + getUrl() + "\">" + getUrl() + "</a>)" : "") + ".");
      }
    } catch (Exception ex) {
    	ex.printStackTrace();
      return ("");
    }
  }
  
  
  /**
   * Tells if this item is a journal article from transitional arguments, not from db.
   * 
   * @return true=in journal, false=something else.
   */
  public boolean isPublishedInJournalFromAttributes() {
    return !journaltitle.equals("") || choosenJournal!=0;
  }

  /**
   * Gives the in: x section of this literature entry (only works if this is a journal entry).
   * 
   * @return The in: x section.
   * @throws NoRowsException
   * @throws TooManyRowsException
   * @throws TorqueException
   */
  public String getJournalData() throws NoRowsException, TooManyRowsException, TorqueException{
	  return getJournalData(true);
  }
  
  public String getJournalData(boolean withurl) throws NoRowsException, TooManyRowsException, TorqueException{
	  return (choosenJournal != 0 ? DBJournalPeer.retrieveByPK(choosenJournal).getNiceTitle() : journaltitle + (journalsubtitle.equals("") ? "" : ". " + journalsubtitle)+(ita.equals("") ? "" : " (" + ita+") ")) + ", vol. " + (volume==0 ? "" : volume + "/") + (year==0 ? "?" : year) + (issue == 0 ? "" : " (issue " + issue + ") ") + ", pp. " + (from==0 ? "?" : from) + " - " + (to==0 ? "?" : to) + (getDoi().equals("") ? "" : " (DOI: "+(withurl ? "<a href=\"derefer.jsp?url=http://dx.doi.org/"+getDoi()+"\">" + getDoi() + "</a>)" : getDoi()+")")) + (withurl && !getUrl().equals("") ? " (URL: <a href=\"" + getUrl() + "\">" + getUrl() + "</a>)" : "");
  }


  /**
   *  Looks if a literature item alreay exists in db.
   *
   * @return                The pk of the entry or null if not yet existing.
   * @exception  TorqueException  Database problems.
   */
  public DBLiterature literatureAlreadyExists(Connection dbconn) throws TorqueException, DataSetException {
    if (type.equals(NmrshiftdbConstants.MONOGRAPH)) {
      List<StringAndStringAndBoolean> list = analyzeAuthor(author);
      StringBuffer sql = new StringBuffer("select LITERATURE.LITERATURE_ID from LITERATURE, ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("IS_AUTHOR as i" + i + ", AUTHOR as a" + i + ", ");
      }
      sql.append("BOOK, BOOK_PUBLISHER, PUBLISHER where LITERATURE.TITLE='" + getTitle() + "' and LITERATURE.SUBTITLE ='" + getSubtitle() + "' and LITERATURE.DOI ='" + getDoi() + "' and LITERATURE.URL='" + getUrl() + "' and LITERATURE.ET_AL ='" + (etalprov ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE) + "' and ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("LITERATURE.LITERATURE_ID =i" + i + ".LITERATURE_ID and i" + i + ".AUTHOR_ID =a" + i + ".AUTHOR_ID and ");
      }
      for (int i = 0; i < list.size(); i++) {
        StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(i);
        sql.append("a" + i + ".NAME ='" + ssb.string1 + "' and a" + i + ".SURNAME ='" + ssb.string2 + "' and ");
      }
      sql.append("LITERATURE.LITERATURE_ID=BOOK.LITERATURE_ID and BOOK.LITERATURE_ID=BOOK_PUBLISHER.LITERATURE_ID and BOOK_PUBLISHER.PUBLISHER_ID=PUBLISHER.PUBLISHER_ID and BOOK_PUBLISHER.YEAR='" + year + "' and ");
      sql.append("PUBLISHER.PLACE='" + place + "' and PUBLISHER.NAME='" + publisher + "';");
      List<Record> l = DBLiteraturePeer.executeQuery(sql.toString(), false, dbconn);
      if (l.size() > 0) {
        return (DBLiteraturePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt()),dbconn));
      }
    }
    if (type.equals(NmrshiftdbConstants.JOURNAL_ARTICLE)) {
      List<StringAndStringAndBoolean> list = analyzeAuthor(author);
      StringBuffer sql = new StringBuffer("select LITERATURE.LITERATURE_ID from LITERATURE, ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("IS_AUTHOR as i" + i + ", AUTHOR as a" + i + ", ");
      }
      sql.append("ARTICLE, JOURNAL_VOLUME, JOURNAL where LITERATURE.TITLE='" + getTitle() + "' and LITERATURE.SUBTITLE ='" + getSubtitle() + "' and LITERATURE.DOI ='" + getDoi() + "' and LITERATURE.URL='" + getUrl() + "' and LITERATURE.ET_AL ='" + (etalprov ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE) + "' and ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("LITERATURE.LITERATURE_ID =i" + i + ".LITERATURE_ID and i" + i + ".AUTHOR_ID =a" + i + ".AUTHOR_ID and ");
      }
      for (int i = 0; i < list.size(); i++) {
        StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(i);
        sql.append("a" + i + ".NAME ='" + ssb.string1 + "' and a" + i + ".SURNAME ='" + ssb.string2 + "' and ");
      }
      sql.append("LITERATURE.LITERATURE_ID=ARTICLE.LITERATURE_ID and ARTICLE.PUBLISHED_IN_JOURNAL=JOURNAL_VOLUME.JOURNAL_VOLUME_ID and JOURNAL_VOLUME.JOURNAL_ID=JOURNAL.JOURNAL_ID and ARTICLE.PAGES_FROM="+from+" and ARTICLE.PAGES_TO="+to+" and JOURNAL_VOLUME.YEAR="+year+" and JOURNAL_VOLUME.VOLUME="+volume+" and ");
      if (choosenJournal == 0) {
        sql.append("JOURNAL.TITLE='"+ journaltitle+"' and JOURNAL.SUBTITLE='"+journalsubtitle+"';");
      } else {
        sql.append("JOURNAL.JOURNAL_ID=" + choosenJournal + ";");
      }
      List<Record> l = DBLiteraturePeer.executeQuery(sql.toString(),false,dbconn);
      if (l.size() > 0) {
        return (DBLiteraturePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt()),dbconn));
      }
    }
    if (type.equals(NmrshiftdbConstants.OTHER_ARTICLE)) {
      List<StringAndStringAndBoolean> list = analyzeAuthor(author);
      StringBuffer sql = new StringBuffer("select LITERATURE.LITERATURE_ID from LITERATURE, ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("IS_AUTHOR as i" + i + ", AUTHOR as a" + i + ", ");
      }
      sql.append("ARTICLE, BOOK, BOOK_PUBLISHER, PUBLISHER where LITERATURE.TITLE='" + getTitle() + "' and LITERATURE.SUBTITLE ='" + getSubtitle() + "' and LITERATURE.DOI ='" + getDoi() + "' and LITERATURE.URL='" + getUrl() + "' and LITERATURE.ET_AL ='" + (etalprov ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE) + "' and ");
      for (int i = 0; i < list.size(); i++) {
        sql.append("LITERATURE.LITERATURE_ID =i" + i + ".LITERATURE_ID and i" + i + ".AUTHOR_ID =a" + i + ".AUTHOR_ID and ");
      }
      for (int i = 0; i < list.size(); i++) {
        StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(i);
        sql.append("a" + i + ".NAME ='" + ssb.string1 + "' and a" + i + ".SURNAME ='" + ssb.string2 + "' and ");
      }
      sql.append("LITERATURE.LITERATURE_ID=ARTICLE.LITERATURE_ID and ARTICLE.PUBLISHED_IN_BOOK=BOOK.LITERATURE_ID and BOOK.LITERATURE_ID=BOOK_PUBLISHER.LITERATURE_ID and BOOK_PUBLISHER.PUBLISHER_ID=PUBLISHER.PUBLISHER_ID and BOOK_PUBLISHER.YEAR='" + year + "' and ARTICLE.PAGES_FROM="+from+" and ARTICLE.PAGES_TO="+to+" and ");
      sql.append("PUBLISHER.PLACE='" + place + "' and PUBLISHER.NAME='" + publisher + "';");
      List<Record> l = DBLiteraturePeer.executeQuery(sql.toString(), false, dbconn);
      if (l.size() > 0) {
        return (DBLiteraturePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt()),dbconn));
      }
    }
    return (null);
  }


  /**
   *  Analyses an author string like a. b; c. d and return a list with name-surname pairs.
   *
   * @param  author  The string to analyze.
   * @return         The map.
   */
  public static List<StringAndStringAndBoolean> analyzeAuthor(String author) {
    ArrayList<StringAndStringAndBoolean> list = new ArrayList<StringAndStringAndBoolean>();
    StringTokenizer st = new StringTokenizer(author, ";,");
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      StringTokenizer localst = new StringTokenizer(line, " ");
      String firstpart = localst.nextToken();
      String secondpart = "";
      if (localst.hasMoreTokens()) {
        secondpart = localst.nextToken();
        while (localst.hasMoreTokens()) {
          firstpart += " " + secondpart;
          secondpart = localst.nextToken();
        }
        StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
        ssb.string1 = secondpart;
        ssb.string2 = firstpart;
        list.add(ssb);
      } else {
        StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
        ssb.string1 = firstpart;
        ssb.string2 = "";
        list.add(ssb);
      }
    }
    return (list);
  }

  
  /**
   * Set the value of Title
   */
   public void setTitle(String v ) 
   {
	   String titleesc=GeneralUtils.escape(v,false);
	   super.setTitle(titleesc);
   }

   
   /**
    * Set the value of Title
    */
    public void setSubtitle(String v ) 
    {
 	   String titleesc=GeneralUtils.escape(v,false);
 	   super.setSubtitle(titleesc);
    }
    /**
     * Set the value of Title
     */
     public void setTitleBook(String v ) 
     {
  	   String titleesc=GeneralUtils.escape(v,false);
  	   titlebook=titleesc;
     }

     
     /**
      * Set the value of Title
      */
      public void setSubtitleBook(String v ) 
      {
   	   String titleesc=GeneralUtils.escape(v,false);
   	   subtitlebook=titleesc;
      }


	public void setAuthor(String author) {
		this.author = GeneralUtils.escape(author,false);
	}

	public void setAuthorbook(String authorbook) {
		this.authorbook = GeneralUtils.escape(authorbook,false);
	}
	
	public void setEditorbook(String editorbook) {
		this.editorbook = GeneralUtils.escape(editorbook,false);
	}
	
	/**
	 * Returns this item as a bibtex entry.
	 * 
	 * @return The bibtex version of this item.
	 * @throws TorqueException 
	 * @throws TooManyRowsException 
	 * @throws NoRowsException 
	 */
	public Element getAsBibtex() throws NoRowsException, TooManyRowsException, TorqueException{
        Element bibtexentry = new Element("entry","http://bibtexml.sf.net/");
        bibtexentry.setNamespaceURI("http://bibtexml.sf.net/");
        Element literatureentry=null;
        if(this.getType().equals(NmrshiftdbConstants.MONOGRAPH)){
            literatureentry = new Element("book","http://bibtexml.sf.net/");
            append("place",this.place,literatureentry);
            append("publisher",this.publisher,literatureentry);
        }else if(this.getType().equals(NmrshiftdbConstants.JOURNAL_ARTICLE)){
            literatureentry = new Element("article","http://bibtexml.sf.net/");
            if(choosenJournal>0){
            	DBJournal journal = DBJournalPeer.retrieveByPK(choosenJournal);
            	append("journal",journal.getTitle()+(journal.getSubtitle()!=null && !journal.getSubtitle().equals("") ? ". "+journal.getSubtitle() : "")+(journal.getIta()!=null && !this.getIta().equals("") ? " ("+this.getIta()+")" : ""),literatureentry);
            }else{
            	append("journal",this.journaltitle+(this.journalsubtitle!=null && !this.journalsubtitle.equals("") ? ". "+this.journalsubtitle : "")+(this.ita!=null && !this.ita.equals("") ? " ("+this.ita+")" : ""),literatureentry);
            }
            append("volume",this.volume+"",literatureentry);
            if(this.from!=0 && this.to!=0)
            	append("pages",this.from+"-"+this.to,literatureentry);
            else if(this.from!=0)
            	append("pages",this.from+"-?",literatureentry);
        }else if(this.getType().equals(NmrshiftdbConstants.OTHER_ARTICLE)){
            literatureentry = new Element("inbook","http://bibtexml.sf.net/");
            if(this.getAuthorbook()!=null)
                append("editor",this.getAuthorbook(),literatureentry);
            append("booktitle",this.getTitlebook(),literatureentry);
            append("place",this.place,literatureentry);
            append("publisher",this.publisher,literatureentry);
            if(this.from!=0 && this.to!=0)
            	append("pages",this.from+"-"+this.to,literatureentry);
            else if(this.from!=0)
            	append("pages",this.from+"-?",literatureentry);
        }
        Element author = new Element("author","http://bibtexml.sf.net/");
        literatureentry.appendChild(author);
        author.appendChild(new Text(this.getAuthor()));
        Element etal = new Element("etal","http://bibtexml.sf.net/");
        literatureentry.appendChild(etal);
        etal.appendChild(new Text(this.etalprov ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE));
        if(this.getTitle()!=null && !this.getTitle().equals("")){
	        Element title = new Element("title","http://bibtexml.sf.net/");
	        literatureentry.appendChild(title);
	        title.appendChild(new Text(this.getTitle()+(this.getSubtitle()!=null && !this.getSubtitle().equals("") ? ". "+this.getSubtitle() : "")));
        }
        Element year = new Element("year","http://bibtexml.sf.net/");
        literatureentry.appendChild(year);
        year.appendChild(new Text(this.getYear()+""));
        if(this.getDoi()!=null && !this.getDoi().equals("")){
            append("doi",this.getDoi(),literatureentry);
        }
        if(this.getUrl()!=null && !this.getUrl().equals("")){
            append("url",this.getUrl(),literatureentry);
        }
        bibtexentry.appendChild(literatureentry);
        return bibtexentry;
	}
	
	  /**
	   * Adds a string in a new child in bibtex namespace to an xml node.
	   * 
	   * @param tag The xml tag for the child.
	   * @param value The value for the child.
	   * @param parent The parent to append to.
	   */
	  private void append(String tag, String value, Element parent){
	      Element year = new Element(tag,"http://bibtexml.sf.net/");
	      parent.appendChild(year);
	      year.appendChild(new Text(value));
	  }
}


