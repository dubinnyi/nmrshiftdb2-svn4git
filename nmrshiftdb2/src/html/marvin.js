//////////////////////////////////////////////////////////////////////////
//// Marvin utility functions
//// Version 3.0.3, Copyright (C) 2000-2003 ChemAxon Ltd., Peter Csizmadia,
////                          Ferenc Csizmadia, Tamas Vertse, Gabor Bartha
//////////////////////////////////////////////////////////////////////////

//
// "Public" parameters that can be specified before msketch_begin/mview_begin.
//

// The MAYSCRIPT attribute for MarvinView.
var mview_mayscript = false;

// Applet names, unspecified by default
var msketch_name = "";
var mview_name = "";
var spectrumapplet_name = "";

// Applet can use these additional jar files. If more then one additional
// file are used, files has to be separated by colon. 
// e.g to generate svg from the applet, the batik-core.jar has to be used.
var msketch_usedJars = "";

// Use "builtin" for the browser's default JVM, "plugin" for the Java Plugin.
var marvin_jvm = "";

// GUI used: "awt" or "swing"
var marvin_gui = "";

//
// Internal functions
//

var marvin_jvm0 = "";
var marvin_gui0 = "";
var applet_type; // depends on marvin_jvm, 0=<applet>, 1=<embed>, 2=<object>

// Set marvin_jvm if the URL of the HTML page has a jvm parameter.
if(location.search.lastIndexOf("jvm=plugin") >= 0) {
	marvin_jvm0 = "plugin";
	if(browser_mozilla_version()==5) {//builtin if Netscape 6-
	    marvin_jvm0 = "builtin";
	}
}
if(location.search.lastIndexOf("jvm=builtin") >= 0) {
	marvin_jvm0 = "builtin";
}

// Set marvin_gui if the location string contains the gui parameter.
if(location.search.lastIndexOf("gui=swing") >= 0) {
	marvin_gui0 = "swing";
}
if(location.search.lastIndexOf("gui=awt") >= 0) {
	marvin_gui0 = "awt";
}

function browser_parse_version0(name) {
	var brz = navigator.userAgent;
	var i = brz.lastIndexOf(name);
	if(i >= 0) {
		var s = brz.substring(i + name.length);
		var j = s.indexOf(".");
		if(j < 0) {
			j = s.indexOf(" ");
		}
		return s.substring(0, j);
	}
	return 0;
}

function browser_parse_version(name) {
	var v = browser_parse_version0(name + "/");
	if(!v) {
		v = browser_parse_version0(name + " ");
	}
	return v;
}

// Returns mozilla version for mozilla and compatible browsers.
function browser_mozilla_version() {
	var s = navigator.userAgent;

	// indexOf is buggy in Netscape 3
	if(s.lastIndexOf("Mozilla/3.") == 0) {
		return 3;
	} else if(s.lastIndexOf("Mozilla/") == 0) {
		return s.substring(8, s.indexOf("."));
	} else {
		return 0;
	}
}

// Returns browser version in Opera, 0 in other browsers.
function browser_Opera() {
	return browser_parse_version("Opera");
}

// Returns mozilla version in Netscape, 0 in other browsers.
function browser_NetscapeMozilla() {
	var brz = navigator.userAgent;
	var compat = brz.toLowerCase().lastIndexOf("compatible") >= 0;
	var opera = browser_Opera();
	if(brz.lastIndexOf("Mozilla/") == 0 && !compat && !opera) {
		return browser_mozilla_version();
	} else {
		return 0;
	}
}

// Returns browser version in MSIE, 0 in other browsers.
function browser_MSIE() {
	var msie = navigator.appName.lastIndexOf("Microsoft Internet Explorer") == 0;
	var opera = browser_Opera();
	if(msie && !opera) {
		return browser_parse_version("MSIE");
	}
	return 0;
}

//Returns the OS version (9 or 10) if it is mac, 0 if isn't.
function macOsVer() {
	var v = navigator.appVersion;
	var vv = navigator.userAgent.toLowerCase();
	var mac = 0;
	if(v.indexOf("Mac") > 0) {
		mac = 9;
		if((vv.indexOf("os x") > 0) || (vv.indexOf("msie 5")) > 0) {
			mac = 10;
		}
	}
	return mac;
}

function marvin_default_jvm()
{
	var v = browser_MSIE();
	if(v >= 6) { // no builtin JVM in MSIE 6
		return "plugin";
	} else { // MSIE 3-5.5 and other browsers
		return "builtin";
	}
}

// Determines default GUI (Swing or AWT) from JVM and browser type.
function marvin_default_gui(jvm)
{
	if(macOsVer() == 9) {
		return "awt"
	} else if(jvm == "plugin") {
		return "swing";
	} else if(navigator.javaEnabled()) {
		// Use Swing in Netscape >= 6 and Opera >= 4
		var opera = browser_Opera();
		var mozver = browser_mozilla_version();
		if(mozver >= 5 || opera >= 4) {
			return "swing";
		}
	}
	return "awt";
}

function applet_begin(jvm, codebase, archive, code, width, height, name, mayscr)
{
	var netscape = browser_NetscapeMozilla();
	var msie = browser_MSIE();
	var opera = browser_Opera();
	applet_type = 0; // <applet>
	if(jvm == "plugin") {
		if(netscape || opera) {
			applet_type = 1; // <embed> in Netscape and Opera
		} else if(msie) {
			applet_type = 2; // <object> in Microsoft
		}
	}
	var s;
	if(applet_type == 1) {
		s = '<embed TYPE="application/x-java-applet;version=1.3"\n';
		s += ' PLUGINSPAGE="http://java.sun.com/products/plugin/1.3/plugin-install.html"\n';
	} else if(applet_type == 2) {
		s = '<object CLASSID="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"\n';
		s += ' CODEBASE="http://java.sun.com/products/plugin/1.3/jinstall-13-win32.cab#Version=1,3,0,0"\n';
	} else {
		s = '<applet';
	}
	if(mayscr) {
		if(applet_type == 0) { // <applet>
			s += ' MAYSCRIPT';
		} else if(applet_type == 1) { // <embed>
			s += ' MAYSCRIPT=true';
		}
	}
	s += ' WIDTH='+width+' HEIGHT='+height;
	if(name) {
		s += ' NAME="'+name+'"';
	}
	s += '\n';
	if(msketch_usedJars != "") { 	
		archive += "," + msketch_usedJars;
	}
	if(applet_type != 2) { // <applet> and <embed>
//		if(applet_type == 0 && netscape == 3) {
//			// use ZIP instead of JAR in Netscape3+<applet>
//			var i = archive.lastIndexOf(".");
//			if(i >= 0) {
//				archive = archive.substring(0, i);
//			}
//			archive += ".zip";
//		}
		s += ' CODEBASE="'+codebase+'" ARCHIVE="'+archive+'" CODE="'+code+'"';
	}
	if(applet_type != 1) { // <applet> and <object>
		s += '>';
	}
	s += '\n';
	if(applet_type == 2) { // <object>
		s += '<param NAME="codebase" VALUE="'+codebase+'">\n';
		s += '<param NAME="archive" VALUE="'+archive+'">\n';
		s += '<param NAME="code" VALUE="'+code+'">\n';
		s += '<param NAME="scriptable" VALUE="true">\n';
		if(mayscr) {
			s += '<param NAME="mayscript" VALUE="true">\n';
		}
	}
	document.write(s);
	return s;
}

function applet_param(name, value)
{
	var s;
	var strvalue = "" + value;
	var has_r = strvalue.lastIndexOf('\r') >= 0;
	var has_n = strvalue.lastIndexOf('\n') >= 0;
	if(has_r || has_n) {
		var M;
		if(has_r) {
			M = strvalue.split("\r");
			if(has_n) {
				M = M.join("").split("\n");
			}
		} else {
			M = strvalue.split("\n");
		}
		value = M.join("\\");
	}
	if(applet_type == 1) { // <embed>
		s = name+'="'+value+'"\n';
	} else { // <applet> and <object>
		s = '<param NAME="'+name+'" VALUE="'+value+'">\n';
	}
	document.write(s);
	return s;
}

function applet_end()
{
	var s;
	var msg = "<center><b>YOU CANNOT SEE A JAVA APPLET HERE</b></center>\n";
	if(applet_type == 1) { // <embed>
		s = '>\n<noembed>\n';
		s += msg;
		s += '</noembed>\n';
	} else if(applet_type == 2) { // <object>
		s += msg;
		s = '</object>\n';
	} else { // <applet>
		s = msg;
		s += '</applet>\n';
	}
	document.write(s);
	return s;
}


//
// "Public" functions
//

// Determine whether signed applet can be used or not.
function can_signed_be_used() {
 	var netscape = browser_NetscapeMozilla();
	var ie = browser_MSIE();
	var opera = browser_Opera();
	var macVer = macOsVer();	
	var can_signed = true;
	marvin_jvm = "builtin";
	if(ie > 0 && !opera && macVer == 0) {	
	    marvin_jvm = "plugin";
	}
	if((0 < netscape && netscape < 5) || (macVer == 9)) {
		can_signed = false;
	}
	return can_signed;
}

// Determine the JVM.
function marvin_get_jvm() {
	var jvm = marvin_jvm0;
	if(!jvm) {
		jvm = (marvin_jvm != "")? marvin_jvm : marvin_default_jvm();
	}
	jvm = jvm.toLowerCase();
	return jvm;
}

// Determine GUI type ("awt" or "swing").
function marvin_get_gui() {
	var gui = marvin_gui0;
	if(!gui) {
		gui = (marvin_gui != "")? marvin_gui : marvin_default_gui(marvin_get_jvm());
	}
	return gui;
}

// If msketch is able to generate image returns 1 else 0. It is depends on 
// the browser.
function msketch_detect() 
{
	var netscape = browser_NetscapeMozilla();
	var msie = browser_MSIE();
	if(msie > 0) {
	    marvin_jvm = "plugin";
	} else if(netscape > 0) {
	    if(netscape > 4) {
	        marvin_jvm = "builtin";
	    } else {
	    	alert("Image generation can be run only in SWING mode.\n"+
			"Your browser does not support SWING.");
		return 0;
	    }
	}
	return 1;
}


function spectrumapplet_begin(codebase, width, height)
{
	var archive, code;
	var jvm = marvin_jvm0;
	if(!jvm) {
		jvm = (marvin_jvm != "")? marvin_jvm : marvin_default_jvm();
	}
	jvm = jvm.toLowerCase();
	archive = "spectrumapplet.jar,jchempaint-applet-core.jar";
	code = "org.openscience.nmrshiftdb.spectrumapplet.SpectrumView";
	applet_begin(jvm, codebase, archive, code, width, height, spectrumapplet_name,
		     true);
}

function spectrumapplet_param(name, value)
{
	return applet_param(name, value);
}

function spectrumapplet_end()
{
	applet_end();
	spectrumapplet_name = "";
}


function msketch_begin(codebase, width, height)
{
  var archive, code;
	var jvm = marvin_jvm0;
	if(!jvm) {
		jvm = (marvin_jvm != "")? marvin_jvm : marvin_default_jvm();
	}
	jvm = jvm.toLowerCase();
	code = "org.openscience.jchempaint.applet.JChemPaintEditorApplet";
    archive = "spectrumapplet.jar,jchempaint-applet-core.jar";
	applet_begin(jvm, codebase, archive, code, width, height, msketch_name,
		     false);
	msketch_param("impliciths", "true");
    msketch_param("codebase_lookup","false");
    msketch_param("onLoadTarget","statusFrame");
    msketch_param("image","hourglass.gif");
    msketch_param("boxborder","false");
    msketch_param("centerimage","true");
    msketch_param("language","en");
}


function msketch_param(name, value)
{
	return applet_param(name, value);
}

function showmol(mol){
	newwin=window.open("nmrshiftdbhtml/showmol.htm","Fenster1","width=310,height=400,left=0,top=0");
    newwin.document.write("<html><body><pre>"+mol+"</pre></body></html>");
}

function msketch_end()
{
	applet_end();
	msketch_name = "";
}

function mview_begin(codebase, width, height)
{
	var archive, code;
	var jvm = marvin_jvm0;
	if(!jvm) {
		jvm = (marvin_jvm != "")? marvin_jvm : marvin_default_jvm();
	}
	jvm = jvm.toLowerCase();
  archive = "spectrumapplet.jar,jchempaint-applet-core.jar";
  code = "org.openscience.jchempaint.applet.JChemPaintViewerApplet";
	applet_begin(jvm, codebase, archive, code, width, height, mview_name,
		     mview_mayscript);
    msketch_param("codebase_lookup","false");
    msketch_param("onLoadTarget","statusFrame");
    msketch_param("image","hourglass.gif");
    msketch_param("boxborder","false");
    msketch_param("centerimage","true");
}

function mview_param(name, value)
{
	return applet_param(name, value);
}

function mview_end()
{
	applet_end();
	mview_name = "";
}

function links_set_search(s) {
	for(i = 0; i < document.links.length; ++i) {
		var p = document.links[i].pathname;
		if(p.lastIndexOf(".html") > 0 || p.lastIndexOf(".jsp") > 0) {
			var href = document.links[i].href;
			var k = href.indexOf('?');
			if(k > 0) {
				href = href.substring(0, k);
			}
			document.links[i].href = href + s;
		}
	}
}

function unix2local(s) {
	var strvalue = "" + s;
	var v = navigator.appVersion;
	if(v.indexOf("Win") > 0) {
		strvalue = strvalue.split("\r\n").join("\n"); // To avoid "\r\r\n"
		return strvalue.split("\n").join("\r\n");
	} else if(v.indexOf("Mac") > 0) { // Macintosh
		return strvalue.split("\n").join("\r");
	} else { // Unix
		return strvalue;
	}
}

function local2unix(s) {
	var strvalue = "" + s;
	var v = navigator.appVersion;
	if(v.indexOf("Win") > 0) {
		return strvalue.split("\r").join("");
	} else if(v.indexOf("Mac") > 0) { // Macintosh
		return strvalue.split("\r").join("\n");
	} else { // Unix
		return strvalue;
	}
}

