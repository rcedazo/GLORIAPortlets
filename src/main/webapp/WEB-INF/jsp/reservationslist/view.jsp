<!-- Copyright (C) 2013 Raquel CEDAZO

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
-->
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet"%>
<%@ taglib uri="http://liferay.com/tld/security"
	prefix="liferay-security"%>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>

<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>

<%@page import="com.liferay.portal.model.User"%>
<%@page import="com.liferay.portal.service.UserLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>

<%@page import="eu.gloria.presentation.liferay.reservations.common.Slot"%>
<%@page import="javax.portlet.PortletPreferences"%>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<!-- http://chuwiki.chuidiang.org/index.php?title=Ejemplo_basico_search-container_en_Liferay  -->
<!-- http://www.abcseo.com/tech/liferay/search-container -->

<portlet:resourceURL var="getExperimentsURL" id="getExperiments">
</portlet:resourceURL>

<portlet:resourceURL var="getTelescopesURL" id="getTelescopes">
</portlet:resourceURL>

<portlet:resourceURL var="getSlotsURL" id="getSlots">
</portlet:resourceURL>

<portlet:resourceURL var="doReserveURL" id="doReserve">
</portlet:resourceURL>

    <script type="text/javascript">

    $(document).ready(function () {
  
        var experimentsUrl = "<%=getExperimentsURL%>";
        var experimentsSource =
        {
            datafields: [
                { name: 'name' }
            ],
            root: "experiments",
            id: 'id',
            datatype: "json",
            async: true,
            url: experimentsUrl
        };
        var experimentsDataAdapter = new $.jqx.dataAdapter(experimentsSource);
        
        var telescopesUrl = "<%=getTelescopesURL%>";
        var telescopesSource =
        {
            datafields: [
                { name: 'name' }
            ],
            root: "telescopes",
            id: 'id',
            datatype: "json",
            url: telescopesUrl,
            async: true
        };
        var telescopesDataAdapter = new $.jqx.dataAdapter(telescopesSource);
        
        var slotsUrl = "<%=getSlotsURL%>";
        var slotsSource =
        {
            datafields: [
                { name: 'begin', type: 'string'} ,
                { name: 'end' , type: 'string'}
            ],
            root: "slots",
            id: 'begin',
            datatype: "json",
            url: slotsUrl,
            async: true
        };
        var slotsDataAdapter = new $.jqx.dataAdapter(slotsSource);

        var rendererDate = function (row, column, value) {
            return value + ' UTC';
        }
        
        // create jqxgrid
        $("#<portlet:namespace />jqxcomboExperiments").jqxComboBox(
        {
            height: '25px',
            width: '250',
            source: experimentsDataAdapter,
            selectedIndex: -1,
			displayMember: "name",
			valueMember: "name"
        });
        
        $("#<portlet:namespace />jqxcomboExperiments").on('change', function (event) {
        	var args = event.args;
        	// $('#<portlet:namespace />jqxgridSlots').jqxGrid('clear');
        	// $("#<portlet:namespace />jqxReserveButton").jqxButton({ disabled: true});
        	if (args) {
        		telescopesSource.data = {experimentSelectedName: args.item.value};
        		$("#<portlet:namespace />jqxcomboTelescopes").jqxComboBox({ disabled: false, source: telescopesDataAdapter});
        	} else {
        		$("#<portlet:namespace />jqxcomboTelescopes").jqxComboBox({ disabled: true, source: null});
        	}
        });
        
        // create jqxgrid
        $("#<portlet:namespace />jqxcomboTelescopes").jqxComboBox(
        {
        	height: '25px',
            width: '250',
            // source: telescopesDataAdapter,
            selectedIndex: -1,
			displayMember: "name",
			valueMember: "name"
        });
        $("#<portlet:namespace />jqxcomboTelescopes").on('change', function (event) {
        	var args = event.args;
        	if (args) {
        		slotsSource.data = {telescopeSelectedName: args.item.value};
        		$("#<portlet:namespace />jqxgridSlots").jqxGrid({ disabled: false, source: slotsSource});
        	} else {
        		$("#<portlet:namespace />jqxgridSlots").jqxGrid({ disabled: true, source: null});
        	}
        });

        $("#<portlet:namespace />jqxgridSlots").jqxGrid(
        {
            width: '100%',
            height: 300,
            //source: slotsSource,
            pageable: true,
            disabled: true,
            columnsresize: true,
            columns: [
                  { text: '<liferay-ui:message key="beginTime" />', datafield: 'begin', width: '50%', cellsrenderer: rendererDate},
                  { text: '<liferay-ui:message key="endTime" />', datafield: 'end', width: '50%', cellsrenderer: rendererDate}
              ]
        });
        $("#<portlet:namespace />jqxgridSlots").on('rowselect', function (event) {
            $("#<portlet:namespace />jqxReserveButton").jqxButton({ disabled: false});
        });
        
    	$("#<portlet:namespace />jqxReserveButton").jqxButton({disabled: true});
    	$("#<portlet:namespace />jqxReserveButton").on('click', function() {
    		var slotSelectedIndex =  $("#<portlet:namespace />jqxgridSlots").jqxGrid('getselectedrowindex');
    		var slotSelected = $('#<portlet:namespace />jqxgridSlots').jqxGrid('getrowdata', slotSelectedIndex);
    		var info = document.getElementById("<portlet:namespace />info");
    		$.ajax({
    			url: "<%=doReserveURL%>",
    			data: {
    				beginDateSelected: slotSelected.begin,
    				endDateSelected: slotSelected.end
    			},
    			type: "POST",
    			dataType: "json",
    			success: function (json) {
    				if (json.success == 'true') {
    					info.className = '';
						info.className = 'portlet-msg-success';
						info.innerHTML = json.message;
	    				var rowid = $('#<portlet:namespace />jqxgridSlots').jqxGrid('getrowid', slotSelectedIndex);
	    				var value = $('#<portlet:namespace />jqxgridSlots').jqxGrid('deleterow', rowid);
    				} else {
    					info.className = '';
    					info.className = 'portlet-msg-error';
    					info.innerHTML = json.message;
    				}
    				$("#<portlet:namespace />jqxReserveButton").jqxButton({disabled: true});
    				$('#<portlet:namespace />jqxgridSlots').jqxGrid('clearselection');
    			}, 
    			error: function(xhr, status) {
    				info.className = '';
					info.className = 'portlet-msg-error';
					info.innerHTML = json.message;
    			}
    		});
    	});    	
    });
    </script>
	
	<!-- Success/error message -->
	<span id="<portlet:namespace />info"></span>
	
    <div id='<portlet:namespace />jqxWidget'>
    	<span><liferay-ui:message key="select-experiment" /></span>
        <div id="<portlet:namespace />jqxcomboExperiments">
        </div>
        <br />
        <span><liferay-ui:message key="select-observatory" /></span>
        <div id="<portlet:namespace />jqxcomboTelescopes" style="margin-top: 10px;">
        </div>
        <br />
        <span><liferay-ui:message key="select-slot" /></span>
        <div id="<portlet:namespace />jqxgridSlots" style="margin-top: 10px;">
        </div>
        <div style="margin-top: 10px;">
        	<input style="margin-left: auto; margin-right: auto; display: block;" type="button" value="<liferay-ui:message key="button-reserve" />" id='<portlet:namespace />jqxReserveButton' />
        </div>
     </div>