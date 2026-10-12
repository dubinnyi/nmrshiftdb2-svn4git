<%--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page import = "java.util.*" %>
<%@ page import = "org.apache.turbine.util.*" %>
<%@ page import = "org.apache.velocity.context.Context" %>
<%@ page import = "org.apache.jetspeed.util.Config" %>
<%@ page import = "org.apache.jetspeed.portal.*" %>
<%@ page import = "org.apache.jetspeed.om.profile.psml.PsmlEntry" %>
<%@ page import = "org.apache.jetspeed.om.SecurityReference" %>
<%@ page import = "org.apache.jetspeed.util.template.*" %>
<%@ page import = "org.apache.jetspeed.util.StringUtils" %>
<%@ page import = "org.apache.jetspeed.services.customlocalization.CustomLocalizationTool" %>

<%
Context context = (Context) request.getAttribute("jspContext");
Object[] columns = (Object[]) context.get("portlets");
PortletController controller = (PortletController) context.get("controller");
Config myConfig = controller.getConfig();
String[] sizes = StringUtils.stringToArray(myConfig.getInitParameter("sizes"), ",");
JetspeedLink jslink = (JetspeedLink) context.get("jslink");
String action = (String) context.get("action");
CustomLocalizationTool l10n = (CustomLocalizationTool) context.get("l10n");
Map portletTitleMap = (Map) context.get("titles");
RunData runData = (RunData) context.get("data");
JetspeedTool jetspeed = (JetspeedTool) context.get("jetspeed");
%>

<style>
.drag-block {
	position: relative;
	border: solid gray;
	cursor: pointer;
	height: 120px;
}
.drag-sink {
	position: relative;
	border: solid #EEEEEE;
}
</style>

<script type="text/javascript" src="javascript/util.js"></script>
<script type="text/javascript">

// global variables

dragBlockTemplate = '<div class="drag-block" name="{col:${col},row:${row}}"><form name="decoration-${portletId}" action="${jslinkTemplate}" method="post" onSubmit="javascript:submitFormWithModelChanges(this)"><table border="0" width="100%"><tr><td align="center" colspan="2"><b>${portletTitle}</b></td><td align="right"><input type="image" src="${closeImageSrc}" border="0" name="eventSubmit_doDelete" title="remove"></td></tr><tr><td valign="top" width="45%">Skin : ${portletSkinName}</td><td valign="top">Security ID : ${portletSecurityId}</td><td></td></tr><tr><td colspan="2">Decoration : ${portletDecoration}</td><td></td></tr></table><input type="hidden" name="js_peid" value="${portletId}"><input type="hidden" name="col" value="${originalCol}"><input type="hidden" name="row" value="${originalRow}"><input type="hidden" name="eventSubmit_doControl" value="doControl"><input type="hidden" name="<%= jslink.getActionKey() %>" value="<%= action %>" ><input type="hidden" name="modelChangeList"></form></div>';

dragSinkTemplate = '<div class="drag-sink" name="{col:${col},row:${row}}"></div>';

layoutPlaygroundTableColumnTemplate = '<td valign="top" width="${width}">${columnContent}</td>';

var modelRoot = [
<%
for (int i = 0; i < columns.length; i++)
{
%>
	<%= i > 0 ? "," : "" %>[
<%
	List portletList = (List) columns[i];
	for (int j = 0; j < portletList.size(); j++)
	{
%>
		<%= j > 0 ? "," : "" %>
<%
		PsmlEntry entry = (PsmlEntry) portletList.get(j);
		String portletId = entry.getId();
		String portletTitle = (String) portletTitleMap.get(entry.getId());
		String portletSkinName = entry.getSkin() == null ? "-- Default --" : entry.getSkin().getName();

		// security reference
		SecurityReference securityReference = jetspeed.getSecurityReference(entry);
		String portletSecurityRef;
		if (securityReference != null)
		{
			portletSecurityRef = securityReference.getParent();
			if (jetspeed.getSecuritySource(entry) == 1)
			{
				portletSecurityRef = portletSecurityRef  + "<span style=\"color:red\"><br/>" + l10n.get("SECURITY_IS_REGISTRY_LEVEL") + ".</span>";
			}
			else if (jetspeed.getSecuritySource(entry) == 2)
			{
				portletSecurityRef = portletSecurityRef  + "<span style=\"color:red\"><br/>" + l10n.get("SECURITY_IS_SYSTEM_DEFAULT") + ".</span>";
			}
		}
		else
		{
			portletSecurityRef = "-- Default --";
		}

		// decoration
		String controlListBox =
			(entry.getControl() != null && entry.getControl().getName() != null) ?
			JetspeedTool.getPortletParameter(runData, (Portlet) runData.getUser().getTemp("customizer"), "control", entry.getControl().getName()) :
			JetspeedTool.getPortletParameter(runData, (Portlet) runData.getUser().getTemp("customizer"), "control");
		controlListBox = controlListBox.substring(12);
		controlListBox = controlListBox.replace('\n', ' ');
		controlListBox = controlListBox.replace('\r', ' ');
%>
		{id:'<%= portletId %>', parent:'<%= entry.getParent() %>',title:'<%= portletTitle %>',description:'<%= entry.getDescription() %>',skinName:'<%= portletSkinName %>',securityId:'<%= portletSecurityRef %>',controlListBox:'<%= controlListBox %>'}
<%
	}
%>
	]
<%
}
%>
];
var sizes = [
<%
for (int i = 0; i < sizes.length; i++)
{
%>
	<%= i > 0 ? "," : "" %>'<%= sizes[i] %>'
<%
}
%>
];

var dragBlockList;
var dragSinkList;

function renderLayoutPlayground(columns)
{
	var content = '<table border="0" width="100%" id="layoutPlaygroundTable"><tr>';

	for (var i = 0; i < columns.length; i++)
	{
		var columnContent = fillTemplate(
			dragSinkTemplate,
			'col', i,
			'row', 0
		);
		var portletList = columns[i];
		for (var j = 0; j < portletList.length; j++)
		{
			var psmlEntry = portletList[j];
			var controlListBox = psmlEntry.controlListBox.replace(/this.form.submit\(\)/gi, 'submitFormWithModelChanges(this.form)');
			columnContent += fillTemplate(
				dragBlockTemplate,
				'portletId', psmlEntry.id,
				'portletTitle', psmlEntry.title,
				'portletSkinName', psmlEntry.skinName,
				'portletSecurityId', psmlEntry.securityId,
				'portletDecoration', controlListBox,
				'closeImageSrc', 'images/close.gif',
				'jslinkTemplate', '<%= jslink.getTemplate() %>',
				'col', i,
				'row', j,
				'originalCol', i,
				'originalRow', j
			);
			columnContent += fillTemplate(
				dragSinkTemplate,
				'col', i,
				'row', j + 1
			);
		}
		content += fillTemplate(
			layoutPlaygroundTableColumnTemplate,
			'width', sizes[i],
			'columnContent', columnContent
		);
	}

	content += '</tr></table>';
	var layoutPlaygroundDiv = document.getElementById('layoutPlayground');
	layoutPlaygroundDiv.innerHTML = content;

	buildDragBlockList();
	buildDragSinkList();
}

var target = null;
var highlightedDragSink = null;
var previousX = 0;
var previousY = 0;

function elementGetTop(elem)
{
	var top = parseInt(elem.style.top);
	if (isNaN(top))
		return 0;
	else
		return top;
}

function elementGetLeft(elem)
{
	var left = parseInt(elem.style.left);
	if (isNaN(left))
		return 0;
	else
		return left;
}

function elementMoveBy(elem, point)
{
	var newTop = elementGetTop(elem) + point.y;
	var newLeft = elementGetLeft(elem) + point.x;
	elem.style.top = newTop + 'px';
	elem.style.left = newLeft + 'px';
}

function elementMoveTo(elem, point)
{
	elem.style.top = point.x + 'px';
	elem.style.left = point.y + 'px';
}

function getEventPositionX(e)
{
	if (!e)
	{
		// IE
		e = window.event;
		return e.clientX + document.body.scrollLeft;
	}
	else
	{
		// standard
		return e.clientX + document.body.scrollLeft;
	}
}

function getEventPositionY(e)
{
	if (!e)
	{
		// IE
		e = window.event;
		return e.clientY + document.body.scrollTop;
	}
	else
	{
		// standard
		return e.clientY + document.body.scrollTop;
	}
}

function findTargetDragBlock(e)
{
	var x = getEventPositionX(e);
	var y = getEventPositionY(e);

	for (var i = 0; i < dragBlockList.length; i++)
	{
		var dragBlock = dragBlockList[i];
		if (elementContainsPoint(dragBlock, x, y))
		{
			return dragBlock;
		}
	}

	return null;
}

function handleMouseDown(event)
{
	if (!event)	// IE
	{
		event = window.event;
//		event.target = event.srcElement;
	}

	target = findTargetDragBlock(event);

	if (target)
	{
		previousX = getEventPositionX(event);
		previousY = getEventPositionY(event);
		target.originalPosition = {x:elementGetLeft(target), y:elementGetTop(target)};
		target.style.borderColor = 'black';
		event.cancelBubble = true;
		event.returnValue = false;
		return false;
	}
	else
	{
		return true;
	}
}

function handleMouseMove(event)
{
	if (!event)	// IE
	{
		event = window.event;
//		event.target = event.srcElement;
	}

	if (target)
	{
		var currentX = getEventPositionX(event);
		var currentY = getEventPositionY(event);
		var dx = currentX - previousX;
		var dy = currentY - previousY;
		target.style.cursor = 'move';
		elementMoveBy(target, {x:dx, y:dy});
		highlightDragSink(currentX, currentY);
		previousX = currentX;
		previousY = currentY;
		event.cancelBubble = true;
		event.returnValue = false;
		return false;
	}
	else
	{
		return true;
	}
}

function handleMouseUp(event)
{
	if (!event)	// IE
	{
		event = window.event;
//		event.target = event.srcElement;
	}

	if (target)
	{
		target.style.cursor = 'pointer';
		target.style.borderColor = 'gray';
		if (highlightedDragSink)
		{
			drop(target, highlightedDragSink);
			unhighlight(highlightedDragSink);
			highlightedDragSink = null;
		}
		else
		{
			elementMoveTo(target, target.originalPosition);
		}
		target.originalPosition = null;
		target = null;
		previousX = 0;
		previousY = 0;
		event.cancelBubble = true;
		event.returnValue = false;
		return false;
	}
	else
	{
		return true;
	}
}

document.onmousedown = handleMouseDown;
document.onmousemove = handleMouseMove;
document.onmouseup = handleMouseUp;

function highlightDragSink(x, y)
{
	for (var i = 0; i < dragSinkList.length; i++)
	{
		var dragSink = dragSinkList[i];
		if (elementContainsPoint(dragSink, x, y))
		{
			if (highlightedDragSink)
				unhighlight(highlightedDragSink);
			highlight(dragSink);
			highlightedDragSink = dragSink;
			return;
		}
	}

	if (highlightedDragSink)
	{
		unhighlight(highlightedDragSink);
		highlightedDragSink = null;
	}
}

function elementContainsPoint(elem, x, y)
{
	var top = elem.offsetTop;
	var left = elem.offsetLeft;
	var height = elem.offsetHeight;
	var width = elem.offsetWidth;
	return	(0 <= x - left && x - left <= width) &&
		(0 <= y - top && y - top <= height);
}

function highlight(elem)
{
	elem.style.borderColor = '#FF0000';
}

function unhighlight(elem)
{
	elem.style.borderColor = '#EEEEEE';
}

function buildDragBlockList()
{
	var layoutPlaygroundTable = document.getElementById('layoutPlaygroundTable');
	var dragBlockCandidateList = layoutPlaygroundTable.getElementsByTagName("div");
	dragBlockList = new Array();
	for (var i = 0; i < dragBlockCandidateList.length; i++)
	{
		var dragBlockCandidate = dragBlockCandidateList[i];
		if (dragBlockCandidate.className == 'drag-block')
			dragBlockList[dragBlockList.length] = dragBlockCandidate;
	}
}

function buildDragSinkList()
{
	var layoutPlaygroundTable = document.getElementById('layoutPlaygroundTable');
	var dragSinkCandidateList = layoutPlaygroundTable.getElementsByTagName("div");
	dragSinkList = new Array();
	for (var i = 0; i < dragSinkCandidateList.length; i++)
	{
		var dragSinkCandidate = dragSinkCandidateList[i];
		if (dragSinkCandidate.className == 'drag-sink')
			dragSinkList[dragSinkList.length] = dragSinkCandidate;
	}
}

function drop(target, dragSink)
{
	var targetRowCol = eval('_rowcol=' + target.attributes['name'].value);
	var dragSinkRowCol = eval('_rowcol=' + dragSink.attributes['name'].value);

	if (destSameAsOrigin(targetRowCol, dragSinkRowCol))
		return;

	// get reference to portletEtnry
	var portletEntry = modelRoot[targetRowCol.col][targetRowCol.row];

	// remove from old position
	modelRoot[targetRowCol.col].splice(targetRowCol.row, 1);

	// insert into new position
	modelRoot[dragSinkRowCol.col].splice(dragSinkRowCol.row, 0, portletEntry);

	recordModelChange(targetRowCol, dragSinkRowCol);

	renderLayoutPlayground(modelRoot);
}

function destSameAsOrigin(origin, dest)
{
	return
		(origin.col == dest.col) &&
		(origin.row == dest.row || origin.row + 1 == dest.row);
}

function recordModelChange(origin, dest)
{
	var change = origin.col + ',' + origin.row + ',' + dest.col + ',' + dest.row;	
	var f = document.forms['xform'];
	if (f.modelChangeList.value == '')
		f.modelChangeList.value = change;
	else
		f.modelChangeList.value += ';' + change;
}

function submitFormWithModelChanges(form)
{
	var modelChangeList = document.forms.xform.modelChangeList.value;
	form.modelChangeList.value = modelChangeList;
	form.submit();
}

function getPrintString(o)
{
	var s = '{';
	for (p in o)
	{
		s += p + ':' + o[p] + ', ';
	}
	s += '}';
	return s;
}
</script>

<!-- layout table -->
<br>

<div id='layoutPlayground'></div>
<script type="text/javascript">
renderLayoutPlayground(modelRoot);
</script>

<br>

<!-- buttons -->
  <form name="xform" action="<%= jslink.getTemplate() %>" method="post">
  <input type="hidden" name="<%= jslink.getActionKey() %>" value="<%= action %>" >
  <input type="hidden" name="modelChangeList" value="">
  <center>
    <table cellspacing="2">
      <tr>
        <td>
          <input type="submit" name="eventSubmit_doSave" value="<%= l10n.get("CUSTOMIZER_SAVEAPPLY") %>">
        </td>
        <td>
          <input type="submit" name="eventSubmit_doCancel" value="<%= l10n.get("CUSTOMIZER_CANCEL") %>">
        </td>
      </tr>
    </table>
  </center>
</form>
