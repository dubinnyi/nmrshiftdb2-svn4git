/* ========================================================================
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

// Title: Timestamp picker
// Description: See the demo at url
// URL: http://us.geocities.com/tspicker/
// Script featured on: http://javascriptkit.com/script/script2/timestamp.shtml
// Version: 1.0
// Date: 12-05-2001 (mm-dd-yyyy)
// Author: Denis Gritcyuk <denis@softcomplex.com>; <tspicker@yahoo.com>
// Notes: Permission given to use this script in any kind of applications if
//    header lines are left unchanged. Feel free to contact the author
//    for feature requests and/or donations

var arr_months = ["January", "February", "March", "April", "May", "June",
                  "July", "August", "September", "October", "November", "December"];

function show_calendar(str_target, str_datetime, str_format) {

   var week_days = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
   var n_weekstart = 1; // day week starts from (normally 0 or 1)

   var dt_datetime = (str_datetime == null || str_datetime =="" ?  new Date() : str2dt(str_datetime, str_format));
   var dt_prev_month = new Date(dt_datetime);
   dt_prev_month.setMonth(dt_datetime.getMonth()-1);
   var dt_next_month = new Date(dt_datetime);
   dt_next_month.setMonth(dt_datetime.getMonth()+1);
   var dt_firstday = new Date(dt_datetime);
   dt_firstday.setDate(1);
   dt_firstday.setDate(1-(7+dt_firstday.getDay()-n_weekstart)%7);
   var dt_lastday = new Date(dt_next_month);
   dt_lastday.setDate(0);

   // html generation (feel free to tune it for your particular application)
   // print calendar header
   var str_buffer = new String (
                               "<html>\n"+
                               "<head>\n"+
                               "	<title>Calendar</title>\n"+
                               "</head>\n"+
                               "<body bgcolor=\"White\">\n"+
                               "<table class=\"clsOTable\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n"+
                               "<tr><td bgcolor=\"#4682B4\">\n"+
                               "<table cellspacing=\"1\" cellpadding=\"3\" border=\"0\" width=\"100%\">\n"+
                               "<tr>\n	<td bgcolor=\"#4682B4\"><a href=\"javascript:window.opener.show_calendar('"+
                               str_target+"', '"+ dt2dtstr(dt_prev_month, str_format)+"', '" + str_format +"');\">&lt;&lt;</a></td>\n"+
                               "	<td bgcolor=\"#4682B4\" colspan=\"5\">"+
                               "<font color=\"white\" face=\"tahoma, verdana\" size=\"2\">"
                               +arr_months[dt_datetime.getMonth()]+" "+dt_datetime.getFullYear()+"</font></td>\n"+
                               "	<td bgcolor=\"#4682B4\" align=\"right\"><a href=\"javascript:window.opener.show_calendar('"
                               +str_target+"', '"+dt2dtstr(dt_next_month, str_format)+"', '" + str_format +"');\">&gt;&gt;</a></td>\n</tr>\n"
                               );

   var dt_current_day = new Date(dt_firstday);
   // print weekdays titles
   str_buffer += "<tr>\n";
   for (var n=0; n<7; n++)
      str_buffer += "	<td bgcolor=\"#87CEFA\">"+
                    "<font color=\"white\" face=\"tahoma, verdana\" size=\"2\">"+
                    week_days[(n_weekstart+n)%7]+"</font></td>\n";
   // print calendar table
   str_buffer += "</tr>\n";
   while (dt_current_day.getMonth() == dt_datetime.getMonth() ||
          dt_current_day.getMonth() == dt_firstday.getMonth()) {
      // print row heder
      str_buffer += "<tr>\n";
      for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
         if (dt_current_day.getDate() == dt_datetime.getDate() &&
             dt_current_day.getMonth() == dt_datetime.getMonth())
            // print current date
            str_buffer += "	<td bgcolor=\"#FFB6C1\" align=\"right\">";
         else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)
            // weekend days
            str_buffer += "	<td bgcolor=\"#DBEAF5\" align=\"right\">";
         else
            // print working days of current month
            str_buffer += "	<td bgcolor=\"white\" align=\"right\">";

         if (dt_current_day.getMonth() == dt_datetime.getMonth())
            // print days of current month
            str_buffer += "<a href=\"javascript:window.opener."+str_target+
                          ".value='"+dt2dtstr(dt_current_day, str_format)+"'; window.close();\">"+
                          "<font color=\"black\" face=\"tahoma, verdana\" size=\"2\">";
         else
            // print days of other months
            str_buffer += "<a href=\"javascript:window.opener."+str_target+
                          ".value='"+dt2dtstr(dt_current_day, str_format)+"'; window.close();\">"+
                          "<font color=\"gray\" face=\"tahoma, verdana\" size=\"2\">";
         str_buffer += dt_current_day.getDate()+"</font></a></td>\n";
         dt_current_day.setDate(dt_current_day.getDate()+1);
      }
      // print row footer
      str_buffer += "</tr>\n";
   }

   var vWinCal = window.open("", "Calendar", 
                             "width=200,height=220,status=no,resizable=no,top=200,left=200");
   vWinCal.opener = self;
   var calc_doc = vWinCal.document;
   calc_doc.write (str_buffer);
   calc_doc.close();
}

// Creates a date from string using regular expressions
function str2dt(str_datetime) {
   var re_date = /^(\d+)\/(\d+)\/(\d+)$/;
   if (!re_date.exec(str_datetime))
      return alert("Invalid Datetime format: "+ str_datetime);
   return(new Date (RegExp.$3, RegExp.$1-1, RegExp.$2));
}

// Creates a date from string using parsing
function str2dt(str_datetime, format) {

   dateFormat=format;

   formatChar = " ";
   aFormat	= dateFormat.split(formatChar);
   if (aFormat.length<3)
   {
      formatChar = "/";
      aFormat	= dateFormat.split(formatChar);
      if (aFormat.length<3)
      {
         formatChar = ".";
         aFormat	= dateFormat.split(formatChar);
         if (aFormat.length<3)
         {
            formatChar = "-";
            aFormat	= dateFormat.split(formatChar);
            if (aFormat.length<3)
            {
               // invalid date	format
               formatChar="";
            }
         }
      }
   }

   tokensChanged =	0;
   if ( formatChar	!= "" );
   {
      // use user's date
      aData =	str_datetime.split(formatChar);

      for	(i=0;i<3;i++)
      {
         if ((aFormat[i]=="d") || (aFormat[i]=="dd"))
         {
            dateSelected = parseInt(aData[i], 10);
            tokensChanged ++;
         }
         else if	((aFormat[i]=="m") || (aFormat[i]=="mm"))
         {
            monthSelected =	parseInt(aData[i], 10) - 1;
            tokensChanged ++;
         }
         else if	(aFormat[i]=="yyyy")
         {
            yearSelected = parseInt(aData[i], 10);
            tokensChanged ++;
         }
         else if	(aFormat[i]=="mmm")
         {
            for	(j=0; j<12;	j++)
            {
               if (aData[i]==arr_months[j].substring(0,3))
               {
                  monthSelected=j;
                  tokensChanged ++;
               }
            }
         }
      }
   }

   if ((tokensChanged!=3)||isNaN(dateSelected)||isNaN(monthSelected)||isNaN(yearSelected))
   {
      today = new Date()
      dateNow = today.getDate()
      monthNow = today.getMonth()
      yearNow = today.getFullYear();

      dateSelected = dateNow;
      monthSelected =	monthNow;
      yearSelected = yearNow;

      alert("Current date [" + str_datetime + "] cannot be parsed according to specified format [" + format + "]. Using current date instead");
   }

   return (new Date(yearSelected, monthSelected, dateSelected));
}

// datetime to date string routine
function dt2dtstrX(dt_datetime, format) {
   return(new String (
                     (dt_datetime.getMonth()+1)+"/"+dt_datetime.getDate()+"/"+dt_datetime.getFullYear()));
}

/**
 * Routine to format a datetime into string based on the format pattern specified.
 * <P>
 * The date format can have three types of separators: hyphen(-), space( ), or slash(/), but must be consistent in their usage. E.g. 
 * d/m/yyyy
 * The acceptable tokens are :
 * <UL>
 * <LI>d - day
 * <LI>dd - day (padded with 0 if less than 10)
 * <LI>m - month (in numbers)
 * <LI>mm - month (in numbers, padded with 0 if less than 10)
 * <LI>mmm - month (in words)
 * <li>yyyy - year 
 * </UL>
 * 
 * @param datetime
 * @param format
 */
function dt2dtstr(dt_datetime, dateFormat) {

   d = dt_datetime.getDate();
   m = dt_datetime.getMonth();
   y = dt_datetime.getFullYear();

   sTmp = dateFormat;
   sTmp = sTmp.replace  ("dd","<e>");
   sTmp = sTmp.replace  ("d","<d>");
   sTmp = sTmp.replace  ("<e>",padZero(d));
   sTmp = sTmp.replace  ("<d>",d);
   sTmp = sTmp.replace  ("mmm","<o>");
   sTmp = sTmp.replace  ("mm","<n>");
   sTmp = sTmp.replace  ("m","<m>");
   sTmp = sTmp.replace  ("<m>",m+1);
   sTmp = sTmp.replace  ("<n>",padZero(m+1));
   sTmp = sTmp.replace  ("<o>",arr_months[m].substring(0,3));

   return sTmp.replace ("yyyy",y);
}

// pad with zeros
function padZero(num) {
   return (num	< 10)? '0' + num : num ;
}

