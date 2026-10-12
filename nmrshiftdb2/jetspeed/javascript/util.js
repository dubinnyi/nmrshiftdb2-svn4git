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

// $Revision: 1.2 $

function isUndefined(v)
{
	return typeof(v) == 'undefined';
}

function isDefined(v)
{
	return typeof(v) != 'undefined';
}

function fillTemplate(template)
{
	var result = template;
	for (var i = 1; i< arguments.length; i+= 2)
	{
		var name = arguments[i];
		var rexp = new RegExp('\\$\\{' + name + '\\}', 'g');
		var value = arguments[i + 1];
		result = result.replace(rexp, value);
	}

	return result;
}

function toggleOpen(modelSource)
{
	var openFlagName = modelSource + '_open';
	var open = eval(openFlagName);
	if (open == undefined) open = false;
	eval(openFlagName + '=' + !open);
}

function refreshElement(id)
{
	var elm = document.getElementById(id);
	if (elm)
	{
		var widget = ElementWidgetMap[id];
		if (widget)
		{
			widget.updateModel();
			elm.innerHTML = widget.render();
		}
	}
}

function isAllCharsEmpty(s)
{
	for (var i = 0; i < s.length; i++)
	{
		if (s.charCodeAt(i) > 32)
			return false;
	}

	return true;
}

function isEmpty(s)
{
	return isUndefined(s) || s == null || s.length == 0 || isAllCharsEmpty(s);
}

function processEmpty(s)
{
	return isEmpty(s) ? '' : s;
}

function nbsp(s)
{
	return isEmpty(s) ? '&nbsp;' : s;
}

function addStaticContentPrefix(rest)
{
	if (isDefined(staticContentPrefix) && staticContentPrefix != null)
		return staticContentPrefix + rest;
	else
		throw "global variable staticContentPrefix undefined or null";
}

function renderWidgetWrapper(widget)
{
	return fillTemplate(
		Template.widgetWrapperElement,
		'elementId', widget.elementId,
		'content', widget.render()
	);
}

function getToday()
{
	var today = new Date();
	return today.getDate() + '/' + MonthMap[today.getMonth()] + '/' + today.getFullYear();
}

var MonthMap = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
