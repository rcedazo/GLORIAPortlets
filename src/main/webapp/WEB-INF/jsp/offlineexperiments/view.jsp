<%
/* Copyright (C) 2013 Raquel CEDAZO

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.theme.ThemeDisplay"%>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.liferay.portal.model.User" %>

<portlet:defineObjects />

<% 
ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
User user = themeDisplay.getUser();
String language = themeDisplay.getLanguageId();

// URL Web Calculator
String experiment1 = "http://venus.datsi.fi.upm.es/venus-webcalc/";
String experiment2 = "http://gloria-dev.s3.amazonaws.com/skydraw/index.html";
String lang = ""; // English by default

// The selection of the language neccesary for the experiment1

if (language.equalsIgnoreCase("es_ES") || language.equalsIgnoreCase("es")) {
	lang="-es";    
} else if (language.equalsIgnoreCase("it_IT") || language.equalsIgnoreCase("it")) {
    lang="-it";
} else if (language.equalsIgnoreCase("ru_RU") || language.equalsIgnoreCase("ru")) {
    lang="-ru";
} else if (language.equalsIgnoreCase("pl_PL") || language.equalsIgnoreCase("pl")) {
    lang="-pl";
} else if (language.equalsIgnoreCase("cs_CS") || language.equalsIgnoreCase("cs")) {
    lang="-cz";
}
%>

<html>
<head>
	<script>
	function showHide(url) {
		var divID = document.getElementById('iframe');
		if(divID.style.display == "") {
            divID.style.display = "none";
		}
    	else {
            divID.style.display = "";
            divID.src = url;
    	}
	}
	</script>
</head>
<body>

<!-- Get list of experiments from WS -->

<div>
	<table id="experimentsTable" class="alternateTable" width="100%" border="0" cellspacing="0" cellpadding="0">
		<thead>
			<tr>
				<th><liferay-ui:message key='name' /></th>
				<th><liferay-ui:message key='description' /></th>
				<th><liferay-ui:message key='author' /></th>
				<th><liferay-ui:message key='scope' /></th>
				<th><liferay-ui:message key='action' /></th>
			</tr>
		</thead>
		<tbody>
				<tr>
					<td><liferay-ui:message key='calculator-earth-sun' /></td>
					<td><liferay-ui:message key='description-calculator-earth-sun' /></td>
					<td>GLORIA</td>
					<td><liferay-ui:message key='scope-educational' /></td>
					<td><input class="aui-button-input" type="button" value="<liferay-ui:message key='button-show' />" onClick = "showHide('<%=experiment1%>data<%=lang%>.php?id=<%=user.getContactId()%>&lang=<%=themeDisplay.getLanguageId()%>'); return false;" /></td>
				</tr>	
				<tr>
					<td><liferay-ui:message key='tracking-objects' /></td>
					<td><liferay-ui:message key='description-tracking-objects' /></td>
					<td>GLORIA</td>
					<td><liferay-ui:message key='scope-scientific' /></td>
					<td><input class="aui-button-input" type="button" value="<liferay-ui:message key='button-show' />" onClick = "showHide('<%= experiment2 %>'); return false;" /></td>
				</tr>					
		</tbody>
	</table>
</div>
<br />

<div>
  <iframe id="iframe" style="display:none;" height="900" width="100%" src=""></iframe>
</div>

</body>