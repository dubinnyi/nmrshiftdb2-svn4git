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

// ===================================================================
// Author: Matt Kruse <matt@mattkruse.com>
// WWW: http://www.mattkruse.com/
//
// NOTICE: You may use this code for any purpose, commercial or
// private, without any further permission from the author. You may
// remove this notice from your final code if you wish, however it is
// appreciated by the author if at least my web site address is kept.
//
// You may *NOT* re-distribute this code in any way except through its
// use. That means, you can include it in your product, or your web
// site, or any other form where the code is actually being used. You
// may not put the plain javascript up on your site for download or
// include it in your javascript libraries for download. 
// If you wish to share this code with others, please just point them
// to the URL instead.
// Please DO NOT link directly to my .js files from your site. Copy
// the files to your server and use them there. Thank you.
// ===================================================================


/* SOURCE FILE: AnchorPosition.js */
function getAncPosition(anchorname){var useWindow=false;var coordinates=new Object();var x=0,y=0;var use_gebi=false, use_css=false, use_layers=false;if(document.getElementById){use_gebi=true;}else if(document.all){use_css=true;}else if(document.layers){use_layers=true;}if(use_gebi && document.all){x=AncPosition_getPageOffsetLeft(document.all[anchorname]);y=AnchorPosition_getPageOffsetTop(document.all[anchorname]);}else if(use_gebi){var o=document.getElementById(anchorname);x=AncPosition_getPageOffsetLeft(o);y=AnchorPosition_getPageOffsetTop(o);}else if(use_css){x=AncPosition_getPageOffsetLeft(document.all[anchorname]);y=AnchorPosition_getPageOffsetTop(document.all[anchorname]);}else if(use_layers){var found=0;for(var i=0;i<document.anchors.length;i++){if(document.anchors[i].name==anchorname){found=1;break;}}if(found==0){coordinates.x=0;coordinates.y=0;return coordinates;}x=document.anchors[i].x;y=document.anchors[i].y;}else{coordinates.x=0;coordinates.y=0;return coordinates;}coordinates.x=x;coordinates.y=y;return coordinates;}
function getAncWindowPosition(anchorname){var coordinates=getAncPosition(anchorname);var x=0;var y=0;if(document.getElementById){if(isNaN(window.screenX)){x=coordinates.x-document.body.scrollLeft+window.screenLeft;y=coordinates.y-document.body.scrollTop+window.screenTop;}else{x=coordinates.x+window.screenX+(window.outerWidth-window.innerWidth)-window.pageXOffset;y=coordinates.y+window.screenY+(window.outerHeight-24-window.innerHeight)-window.pageYOffset;}}else if(document.all){x=coordinates.x-document.body.scrollLeft+window.screenLeft;y=coordinates.y-document.body.scrollTop+window.screenTop;}else if(document.layers){x=coordinates.x+window.screenX+(window.outerWidth-window.innerWidth)-window.pageXOffset;y=coordinates.y+window.screenY+(window.outerHeight-24-window.innerHeight)-window.pageYOffset;}coordinates.x=x;coordinates.y=y;return coordinates;}
function AncPosition_getPageOffsetLeft(el){var ol=el.offsetLeft;while((el=el.offsetParent) != null){ol += el.offsetLeft;}return ol;}
function AncPosition_getWindowOffsetLeft(el){return AncPosition_getPageOffsetLeft(el)-document.body.scrollLeft;}
function AnchorPosition_getPageOffsetTop(el){var ot=el.offsetTop;while((el=el.offsetParent) != null){ot += el.offsetTop;}return ot;}
function AncPosition_getWindowOffsetTop(el){return AnchorPosition_getPageOffsetTop(el)-document.body.scrollTop;}

/* SOURCE FILE: PopupWindow2.js */
function Popup_getXYPosition(anchorname){var coordinates;if(this.type == "WINDOW"){coordinates = getAncWindowPosition(anchorname);}else{coordinates = getAncPosition(anchorname);}this.x = coordinates.x;this.y = coordinates.y;}
function Popup_setSize(width,height){this.width = width;this.height = height;}
function Popup_populate(contents){this.contents = contents;this.populated = false;}
function Popup_refresh(){if(this.divName != null){if(this.use_gebi){document.getElementById(this.divName).innerHTML = this.contents;}else if(this.use_css){document.all[this.divName].innerHTML = this.contents;}else if(this.use_layers){var d = document.layers[this.divName];d.document.open();d.document.writeln(this.contents);d.document.close();}}else{if(this.PopupWindow2 != null && !this.PopupWindow2.closed){this.PopupWindow2.document.open();this.PopupWindow2.document.writeln(this.contents);this.PopupWindow2.document.close();this.PopupWindow2.focus();}}}
function Popup_showPopup(anchorname){this.getXYPosition(anchorname);this.x += this.offsetX;this.y += this.offsetY;if(!this.populated &&(this.contents != "")){this.populated = true;this.refresh();}if(this.divName != null){if(this.use_gebi){document.getElementById(this.divName).style.left = this.x;document.getElementById(this.divName).style.top = this.y;document.getElementById(this.divName).style.visibility = "visible";}else if(this.use_css){document.all[this.divName].style.left = this.x;document.all[this.divName].style.top = this.y;document.all[this.divName].style.visibility = "visible";}else if(this.use_layers){document.layers[this.divName].left = this.x;document.layers[this.divName].top = this.y;document.layers[this.divName].visibility = "visible";}}else{if(this.PopupWindow2 == null || this.PopupWindow2.closed){if(screen && screen.availHeight){if((this.y + this.height) > screen.availHeight){this.y = screen.availHeight - this.height;}}if(screen && screen.availWidth){if((this.x + this.width) > screen.availWidth){this.x = screen.availWidth - this.width;}}this.PopupWindow2 = window.open("about:blank","window_"+anchorname,"toolbar=no,location=no,status=no,menubar=no,scrollbars=auto,resizable,alwaysRaised,dependent,titlebar=no,width="+this.width+",height="+this.height+",screenX="+this.x+",left="+this.x+",screenY="+this.y+",top="+this.y+"");}this.refresh();}}
function Popup_hidePopup(){if(this.divName != null){if(this.use_gebi){document.getElementById(this.divName).style.visibility = "hidden";}else if(this.use_css){document.all[this.divName].style.visibility = "hidden";}else if(this.use_layers){document.layers[this.divName].visibility = "hidden";}}else{if(this.PopupWindow2 && !this.PopupWindow2.closed){this.PopupWindow2.close();this.PopupWindow2 = null;}}}
function Popup_isClicked(e){if(this.divName != null){if(this.use_layers){var clickX = e.pageX;var clickY = e.pageY;var t = document.layers[this.divName];if((clickX > t.left) &&(clickX < t.left+t.clip.width) &&(clickY > t.top) &&(clickY < t.top+t.clip.height)){return true;}else{return false;}}else if(document.all){var t = window.event.srcElement;while(t.parentElement != null){if(t.id==this.divName){return true;}t = t.parentElement;}return false;}else if(this.use_gebi){var t = e.originalTarget;while(t.parentNode != null){if(t.id==this.divName){return true;}t = t.parentNode;}return false;}return false;}return false;}
function Popup_hideIfNotClicked(e){if(this.autoHideEnabled && !this.isClicked(e)){this.hidePopup();}}
function Popup_autoHide(){this.autoHideEnabled = true;}
function Popup_hidePopupWindow2s(e){for(var i=0;i<PopupWindow2Objects.length;i++){if(PopupWindow2Objects[i] != null){var p = PopupWindow2Objects[i];p.hideIfNotClicked(e);}}}
function Popup_attachListener(){if(document.layers){document.captureEvents(Event.MOUSEUP);}window.PopupWindow2OldEventListener = document.onmouseup;if(window.PopupWindow2OldEventListener != null){document.onmouseup = new Function("window.PopupWindow2OldEventListener();Popup_hidePopupWindow2s();");}else{document.onmouseup = Popup_hidePopupWindow2s;}}
function PopupWindow2(){if(!window.PopupWindow2Index){window.PopupWindow2Index = 0;}if(!window.PopupWindow2Objects){window.PopupWindow2Objects = new Array();}if(!window.listenerAttached){window.listenerAttached = true;Popup_attachListener();}this.index = PopupWindow2Index++;PopupWindow2Objects[this.index] = this;this.divName = null;this.PopupWindow2 = null;this.width=0;this.height=0;this.populated = false;this.visible = false;this.autoHideEnabled = false;this.contents = "";if(arguments.length>0){this.type="DIV";this.divName = arguments[0];}else{this.type="WINDOW";}this.use_gebi = false;this.use_css = false;this.use_layers = false;if(document.getElementById){this.use_gebi = true;}else if(document.all){this.use_css = true;}else if(document.layers){this.use_layers = true;}else{this.type = "WINDOW";}this.offsetX = 0;this.offsetY = 0;this.getXYPosition = Popup_getXYPosition;this.populate = Popup_populate;this.refresh = Popup_refresh;this.showPopup = Popup_showPopup;this.hidePopup = Popup_hidePopup;this.setSize = Popup_setSize;this.isClicked = Popup_isClicked;this.autoHide = Popup_autoHide;this.hideIfNotClicked = Popup_hideIfNotClicked;}


/* SOURCE FILE: ColorSwatch2.js */

ColorSwatch_targetInput = null;
function ColorSwatch_writeDiv(){document.writeln("<DIV ID=\"ColorSwatchDiv\" STYLE=\"position:absolute;visibility:hidden;\"> </DIV>");}
function ColorSwatch_show(anchorname){this.showPopup(anchorname);}
function ColorSwatch_pickSwatch(color,obj){obj.hidePopup();pickSwatch(color);}
function pickSwatch(color){if(ColorSwatch_targetInput==null){alert("Target Input is null, which means you either didn't use the 'select' function or you have no defined your own 'pickSwatch' function to handle the picked color!");return;}ColorSwatch_targetInput.value = color;}

function ColorSwatch_select(inputobj,linkname)
{

	if(inputobj.type!="text" && inputobj.type!="hidden" && inputobj.type!="textarea")
	{
		alert("ColorSwatch.select: Input object passed is not a valid form input object");
		window.ColorSwatch_targetInput=null;return;
	}
	window.ColorSwatch_targetInput = inputobj;
	this.show(linkname);
}

function ColorSwatch_highlightColor(c){var thedoc =(arguments.length>1)?arguments[1]:window.document;var d = thedoc.getElementById("ColorSwatchSelectedColor");d.style.backgroundColor = c;d = thedoc.getElementById("ColorSwatchSelectedColorValue");d.innerHTML = c;}
function ColorSwatch()
{
	var windowMode = false;
	
	if(arguments.length==0)
	{
		var divname = "ColorSwatchDiv";
	}else if(arguments[0] == "window")
	{
		var divname = '';
		windowMode = true;
	}else
	{
		var divname = arguments[0];
	}
	
	if(divname != "")
	{
		var cs = new PopupWindow2(divname);
	}else
	{
		var cs = new PopupWindow2();
		cs.setSize(250, 225);
	}
	cs.currentValue = "#FFFFFF";
	cs.writeDiv = ColorSwatch_writeDiv;
	cs.highlightColor = ColorSwatch_highlightColor;
	cs.show = ColorSwatch_show;
	cs.select = ColorSwatch_select;
	var colors = new Array("000000","000033","000066","000099","0000CC","0000FF","330000","330033","330066","330099","3300CC",
"3300FF","660000","660033","660066","660099","6600CC","6600FF","990000","990033","990066","990099",
"9900CC","9900FF","CC0000","CC0033","CC0066","CC0099","CC00CC","CC00FF","FF0000","FF0033","FF0066",
"FF0099","FF00CC","FF00FF","003300","003333","003366","003399","0033CC","0033FF","333300","333333",
"333366","333399","3333CC","3333FF","663300","663333","663366","663399","6633CC","6633FF","993300",
"993333","993366","993399","9933CC","9933FF","CC3300","CC3333","CC3366","CC3399","CC33CC","CC33FF",
"FF3300","FF3333","FF3366","FF3399","FF33CC","FF33FF","006600","006633","006666","006699","0066CC",
"0066FF","336600","336633","336666","336699","3366CC","3366FF","666600","666633","666666","666699",
"6666CC","6666FF","996600","996633","996666","996699","9966CC","9966FF","CC6600","CC6633","CC6666",
"CC6699","CC66CC","CC66FF","FF6600","FF6633","FF6666","FF6699","FF66CC","FF66FF","009900","009933",
"009966","009999","0099CC","0099FF","339900","339933","339966","339999","3399CC","3399FF","669900",
"669933","669966","669999","6699CC","6699FF","999900","999933","999966","999999","9999CC","9999FF",
"CC9900","CC9933","CC9966","CC9999","CC99CC","CC99FF","FF9900","FF9933","FF9966","FF9999","FF99CC",
"FF99FF","00CC00","00CC33","00CC66","00CC99","00CCCC","00CCFF","33CC00","33CC33","33CC66","33CC99",
"33CCCC","33CCFF","66CC00","66CC33","66CC66","66CC99","66CCCC","66CCFF","99CC00","99CC33","99CC66",
"99CC99","99CCCC","99CCFF","CCCC00","CCCC33","CCCC66","CCCC99","CCCCCC","CCCCFF","FFCC00","FFCC33",
"FFCC66","FFCC99","FFCCCC","FFCCFF","00FF00","00FF33","00FF66","00FF99","00FFCC","00FFFF","33FF00",
"33FF33","33FF66","33FF99","33FFCC","33FFFF","66FF00","66FF33","66FF66","66FF99","66FFCC","66FFFF",
"99FF00","99FF33","99FF66","99FF99","99FFCC","99FFFF","CCFF00","CCFF33","CCFF66","CCFF99","CCFFCC",
"CCFFFF","FFFF00","FFFF33","FFFF66","FFFF99","FFFFCC","FFFFFF");var total = colors.length;var width = 18;var cs_contents = "";var windowRef =(windowMode)?"window.opener.":"";if(windowMode){cs_contents += "<HTML><HEAD><TITLE>Select Color</TITLE></HEAD>";cs_contents += "<BODY MARGINWIDTH=0 MARGINHEIGHT=0 LEFMARGIN=0 TOPMARGIN=0><CENTER>";}cs_contents += "<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=1 BGCOLOR=FFFFFF>";var use_highlight =(document.getElementById || document.all)?true:false;for(var i=0;i<total;i++){if((i % width) == 0){cs_contents += "<TR>";}if(use_highlight){var mo = 'onMouseOver="'+windowRef+'ColorSwatch_highlightColor(\''+colors[i]+'\',window.document)"';}else{mo = "";}cs_contents += '<TD BGCOLOR="'+colors[i]+'"><FONT SIZE="-3"><A HREF="#" onClick="'+windowRef+'ColorSwatch_pickSwatch(\''+colors[i]+'\','+windowRef+'window.PopupWindow2Objects['+cs.index+']);return false;" '+mo+' STYLE="text-decoration:none;">&nbsp;&nbsp;&nbsp;</A></FONT></TD>';if( ((i+1)>=total) ||(((i+1) % width) == 0)){cs_contents += "</TR>";}}if(document.getElementById){var width1 = Math.floor(width/2);var width2 = width = width1;cs_contents += "<TR><TD COLSPAN='"+width1+"' BGCOLOR='#ffffff' ID='ColorSwatchSelectedColor'>&nbsp;</TD><TD COLSPAN='"+width2+"' ALIGN='CENTER' ID='ColorSwatchSelectedColorValue'>#FFFFFF</TD></TR>";}cs_contents += "</TABLE>";if(windowMode){cs_contents += "</CENTER></BODY></HTML>";}cs.populate(cs_contents+"\n");cs.offsetY = 25;cs.autoHide();return cs;}

