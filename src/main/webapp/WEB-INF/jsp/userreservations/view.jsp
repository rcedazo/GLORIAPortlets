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

<%@page import="javax.servlet.jsp.jstl.core.LoopTagStatus"%>
<%@page import="eu.gloria.presentation.liferay.reservations.common.Reservation"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet"%>
<%@ taglib uri="http://liferay.com/tld/security" prefix="liferay-security"%>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util"%>

<%@page import="java.util.List"%>

<portlet:defineObjects />

<portlet:resourceURL var="cancelReservationURL" id="cancelReservation">
	<portlet:param name="operationType" value="cancel" />
</portlet:resourceURL>

<portlet:resourceURL var="goExperimentURL" id="goExperiment">
</portlet:resourceURL>

<portlet:renderURL var="updateReservationsURL">
    <portlet:param name="jspPage" value="/view.jsp" />
</portlet:renderURL>

<script>
	function <portlet:namespace />cancelReservation(reservationId, rowSelected) {

		var info = document.getElementById("<portlet:namespace />info");

		var table = document
				.getElementById("<portlet:namespace />reservationTable");

		AUI()
				.use(
						'aui-io-request',
						function(A) {
							var url = '<c:out escapeXml="false" value="${cancelReservationURL}" />';
							A.io
									.request(
											url,
											{
												method : 'POST',
												data : {
													reservationId : reservationId
												},
												dataType : 'json',
												on : {
													failure : function() {
														info.innerHTML = '<liferay-ui:message key="msg-error"/>';
													},
													success : function() {
														var response = this.get('responseData');
														if (response.success == true) {
															info.className = '';
															info.className = 'portlet-msg-success';
															info.innerHTML = response.message;
															// Remove the reservation from the table
															// If it is ok, remove the reservation from the table
															var rowCount = table.rows.length;
															for ( var i = 1; i < rowCount; i++) {
																var row = table.rows[i];
																if (row.cells[0].innerHTML == rowSelected) {
																	table.deleteRow(i);
																	return;
																}
															}
														} else { // Error cancelling the reservation
															info.className = '';
															info.className = 'portlet-msg-error';
															info.innerHTML = response.message;
														}
													}
												}
											});
						});
	}

	function <portlet:namespace />goToExperiment(reservationId) {
		var info = document.getElementById("<portlet:namespace />info");
		AUI()
		.use(
				'aui-io-request',
				function(A) {
					var url = '<c:out escapeXml="false" value="${goExperimentURL}" />';
					A.io
							.request(
									url,
									{
										method : 'POST',
										data : {
											reservationId : reservationId
										},
										dataType : 'json',
										on : {
											failure : function() {
												info.innerHTML = '<liferay-ui:message key="msg-error"/>';
											},
											success : function() {
												var response = this.get('responseData');
												if (response.success == true) {
													window.location.replace(response.url);
												} else {
													info.className = '';
													info.className = 'portlet-msg-error';
													info.innerHTML = response.message;
												}
											}
										}
									});
				});
		
	}
</script>

<!-- Success/error message -->
<span id="<portlet:namespace />info"> </span>

<!-- If there are some reservations -->
<c:choose>
	<c:when test="${userReservations != null}">
		<div>
			<table id="<portlet:namespace />reservationTable" class="alternateTable">
				<thead>
					<tr>
						<th class="hidden">ID</th>
						<th><liferay-ui:message key='beginTime' /></th>
						<th><liferay-ui:message key='endTime' /></th>
						<th><liferay-ui:message key='experiment' /></th>
						<th><liferay-ui:message key='telescopes' /></th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="rsv" items="${userReservations}" varStatus="status">
						<tr>
							<fmt:setLocale value="${pageContext.request.locale}" />
							<td class="hidden"><c:out value="${status.index}" /></td>
							<td><fmt:formatDate pattern="EEE, d MMM yyyy HH:mm:ss"
									value="${rsv.slot.begin}" /> UTC</td>
							<td><fmt:formatDate pattern="EEE, d MMM yyyy HH:mm:ss"
									value="${rsv.slot.end}" /> UTC</td>
							<td><c:out value="${rsv.experiment}" /></td>
							<td><c:out value="${rsv.telescopes}" /></td>
							<td><liferay-ui:icon-menu>
									<liferay-ui:icon-delete
										url='<%="javascript:" + renderResponse.getNamespace() + 
										"cancelReservation("+((Reservation)pageContext.getAttribute("rsv")).getReservationId() + ", " + ((LoopTagStatus)pageContext.getAttribute("status")).getIndex() + ");"%>' />

									<liferay-ui:icon image="view" message="button-go-experiment"
										url='<%="javascript:" + renderResponse.getNamespace() + "goToExperiment("+((Reservation)pageContext.getAttribute("rsv")).getReservationId() + ");"%>' />
								</liferay-ui:icon-menu>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</c:when>
	<c:when test="${userReservations == null}">
		<p class="portlet-msg-info">
			<liferay-ui:message key='msg-error-not-reservations' />
		</p>
	</c:when>
</c:choose>

<br />

<div style="margin-top: 10px;">
  	<input style="margin-left: auto; margin-right: auto; display: block;" type="button" value="<liferay-ui:message key="update-reservations" />"  onclick="location.href='<%= updateReservationsURL %>'" />
</div>