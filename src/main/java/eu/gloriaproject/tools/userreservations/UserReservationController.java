/*  Copyright (C) 2013 Raquel CEDAZO

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

package eu.gloriaproject.tools.userreservations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;

import eu.gloria.gs.services.experiment.online.OnlineExperimentException;
import eu.gloria.gs.services.experiment.online.data.NoSuchExperimentException;
import eu.gloria.gs.services.experiment.online.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.online.operations.NoSuchOperationException;
import eu.gloria.gs.services.experiment.online.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.experiment.online.reservation.NoReservationsAvailableException;
import eu.gloria.gs.services.experiment.online.reservation.NoSuchReservationException;
import eu.gloria.presentation.liferay.reservations.common.Reservation;
import eu.gloria.presentation.liferay.reservations.services.ReservationsService;
import eu.gloria.presentation.liferay.reservations.services.exceptions.ConnectionException;

@Controller("reservationListController")
@RequestMapping("VIEW")
public class UserReservationController {

	private static final Log log = LogFactory.getLog(UserReservationController.class);
	
    private long experimentPlid = -1;
	
	@Resource(name = "reservationsService")
	private ReservationsService reservationsService;
	
	public void setReservationsService(ReservationsService reservationsService) {
		this.reservationsService = reservationsService;
	}
	
	@RequestMapping(value="VIEW")
	public ModelAndView showInit(RenderRequest renderRequest, RenderResponse response) {
        log.info("Ejecutando showInit");

        // Get email and password of the current user
        ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
        ResourceBundle rb = ResourceBundle.getBundle( "Language", renderRequest.getLocale());

        try {
            // Get the list of reservations from the Service
            List<Reservation> userReservations;
			userReservations = reservationsService.getUserReservations(emailUser, passwordUser);
	        // Set the list of reservations as an attribute
	        Map<String, Object> model = new HashMap<String, Object>();
	        model.put("userReservations", userReservations);
	        return new ModelAndView("userreservations/view", model);
		} catch (OnlineExperimentException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (NoReservationsAvailableException e) {
			// Exception: Not pending reservations
            log.error(rb.getString("msg-error-not-reservations"));
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage", rb.getString("msg-error-not-reservations"));
            return new ModelAndView("userreservations/view", model);
		} catch (ConnectionException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (ExperimentOperationException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (NoSuchOperationException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (ExperimentParameterException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (ExperimentNotInstantiatedException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (NoSuchReservationException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		} catch (NoSuchExperimentException e) {
            log.error(e.getMessage());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("errorMessage",  e.getMessage());
            return new ModelAndView("error", model);
		}
        
    }

    /**
     * To get the current user's reservations (by AJAX)
     */
	@ResourceMapping(value="cancelReservation")
    public void cancelReservation(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

        ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        String reservationId = request.getParameter("reservationId");

        log.info("Cancelling reservationId = " + reservationId);

        // Get email and password of the current user
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
        // Call to the Reservations Service        
        try {
			reservationsService.cancelReservation(emailUser, passwordUser, Integer.parseInt(reservationId));
	    	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
	    	jsonObject.put("success", true);
	    	jsonObject.put("message", rb.getString("msg-reservation-cancelled"));
	    	PrintWriter writer = response.getWriter();
	    	writer.write(jsonObject.toString());
		} catch (NumberFormatException e) {
			log.error(rb.getString("msg-error-invalid-format"));
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error-invalid-format"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (OnlineExperimentException e) {
        	log.error("OnlineExperimentError");
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (NoSuchReservationException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (ConnectionException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (ExperimentOperationException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (NoSuchOperationException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (ExperimentParameterException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (ExperimentNotInstantiatedException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		} catch (NoSuchExperimentException e) {
        	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        	jsonObject.put("success", false);
        	jsonObject.put("message", rb.getString("msg-error"));
        	PrintWriter writer = response.getWriter();
        	writer.write(jsonObject.toString());
        	return;
		}

    }
	
	/**
     * To go to the Experiment Panel (Private Page)
     */
	@ResourceMapping(value="goExperiment")
    public void goExperiment(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

        ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        String reservationId = request.getParameter("reservationId");
        
        // Get email and password of the current user
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
			
        try {
			reservationsService.getExperimentInformation(emailUser, passwordUser, Integer.parseInt(reservationId));
	        // TODO Set the experiment selected
			String friendlyURL = renderExperiment(request, response, reservationId, "SOLAR");	
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", true);
	        jsonObject.put("message", rb.getString("msg-experiment-created"));
	        jsonObject.put("url", friendlyURL);
	        PrintWriter writer = response.getWriter();
	        writer.write(jsonObject.toString());
		} catch (NumberFormatException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (OnlineExperimentException e) {
			log.error("No reservation for "+reservationId+":"+e.getMessage());
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error-not-reservations"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (ExperimentNotInstantiatedException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error-not-instantiated"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
			log.error("No reservation for "+reservationId+":"+e.getMessage());
		} catch (NoSuchReservationException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error-current-reservation"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (ConnectionException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (ExperimentOperationException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (NoSuchOperationException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (ExperimentParameterException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		} catch (NoSuchExperimentException e) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("success", false);
            jsonObject.put("message", rb.getString("msg-error"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonObject.toString());
		}
        
	}
	
	/**
     * Render an experiment, adding a new layer and painting webcomponents
     * @param request
     * @param response
     */
	private String renderExperiment(ResourceRequest request, ResourceResponse response, String reservationId, String experimentName) {

		log.info("Adding page for reservation "+reservationId);
		Layout lay = null;

		String url = "/user";
		String friendlyURL = "/experiment_"+experimentName;

		boolean privateLayout = true;
		long parentLayoutId = 0;
		String title = null;
		String description = null;
		String type = LayoutConstants.TYPE_PORTLET;
		boolean hidden = false;

		ServiceContext serviceContext = new ServiceContext();
		
		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(com.liferay.portal.kernel.util.WebKeys.THEME_DISPLAY);
		
			
		Layout layout = themeDisplay.getLayout();

		long userId = Long.parseLong(request.getRemoteUser());
		
		long groupId = -1;

		try{
			
			Group group = GroupLocalServiceUtil.getGroup(layout.getCompanyId(), request.getRemoteUser());
			groupId = group.getGroupId();
			url = url + group.getFriendlyURL();
			
			lay = LayoutLocalServiceUtil.addLayout(userId, groupId,
					privateLayout, parentLayoutId, experimentName, title,
					description, type, hidden, friendlyURL, serviceContext);
	
			experimentPlid = lay.getPlid();
			log.info("Page with id="+lay.getPlid()+" added");
			UnicodeProperties properties = new UnicodeProperties();
			properties.setProperty("layout-template-id", "3_columns");
			properties.setProperty("show-alternate-links", "true");
			properties.setProperty("layoutUpdateable", "true");

			lay.setThemeId(layout.getThemeId());
			lay.setTypeSettingsProperties(properties);

			LayoutLocalServiceUtil.updateLayout(lay);
			
			LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) lay
					.getLayoutType();
			layoutTypePortlet.setLayoutTemplateId(userId,
					((LayoutTypePortlet) lay.getLayoutType())
							.getLayoutTemplateId());
			


			// PORLETS OF TAD
			// EXPERIMENT---------------------------------------------------------------

			// Add webcam portlet
						log.info("adding surveillance camera portlet");
						
						//webcam portlet
						String webcamPortletId = layoutTypePortlet.addPortletId(	   
						Long.parseLong(request.getRemoteUser()),	
						"GLORIASurveillanceCamera_WAR_GLORIASurveillanceCameraportlet",	
						"column-1", -1);
						 
						 Properties webcamPreferences = new Properties();
						 webcamPreferences.put("showBrightness","0");
						 webcamPreferences.put("showGain","0");
						 webcamPreferences.put("showExposure","0");
						 webcamPreferences.put("showContrast","0");
						 webcamPreferences.put("showTakeImage","0");
						 webcamPreferences.put("showContinousMode","0");
						 webcamPreferences.put("configured", "true");
						 webcamPreferences.put("reservationId",String.valueOf(reservationId));
//						 webcamPreferences.put("operation","load_scam_0");
//						 webcamPreferences.put("operation_parameter","scam_0_url");
						 webcamPreferences.put("parameterName", "stream_url00");
						 

						 configurePortlet(themeDisplay.getCompanyId(),
						 PortletKeys.PREFS_OWNER_ID_DEFAULT,
						 PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
						 lay.getPlid(), webcamPortletId, webcamPreferences);
						 log.info("webcam surveillance camera portlet added");

						 
			// Add dome Portlet
			// String domePortletId = layoutTypePortlet.addPortletId(
			// Long.parseLong(request.getRemoteUser()),
			// "GLORIADome_WAR_GLORIADomeportlet", "column-1", -1);

			/* Properties webComponentPreferences = new Properties();
			webComponentPreferences.put("showControlPanel", "1");
			webComponentPreferences.put("showOpenButton", "1");
			webComponentPreferences.put("showCloseButton", "1");
			webComponentPreferences.put("configured", "true");
			webComponentPreferences.put("reservationId",
					String.valueOf(reservationId)); */
			// webComponentPreferences.put("showImagePanel", "true");

			/*configurePortlet(themeDisplay.getCompanyId(),
					PortletKeys.PREFS_OWNER_ID_DEFAULT,
					PortletKeys.PREFS_OWNER_TYPE_LAYOUT, lay.getPlid(),
					domePortletId, webComponentPreferences);*/

			// Surveillance camera 2 portlet
						 log.info("adding surveillance camera 2 portlet");
						 String webcam2PortletId = layoutTypePortlet.addPortletId(
						Long.parseLong(request.getRemoteUser()),	"GLORIASurveillanceCamera_WAR_GLORIASurveillanceCameraportlet",
						"column-1", -1);
						 
						 Properties webcam2Preferences = new Properties();
						 webcam2Preferences.put("showBrightness","0");
						 webcam2Preferences.put("showGain","0");
						 webcam2Preferences.put("showExposure","0");
						 webcam2Preferences.put("showContrast","0");
						 webcam2Preferences.put("showTakeImage","0");
						 webcam2Preferences.put("showContinousMode","0");
						 webcam2Preferences.put("configured", "true");
						webcam2Preferences.put("reservationId",String.valueOf(reservationId));
						webcam2Preferences.put("parameterName", "stream_url01");
						 

						 configurePortlet(themeDisplay.getCompanyId(),
						 PortletKeys.PREFS_OWNER_ID_DEFAULT,
						 PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
						 lay.getPlid(), webcam2PortletId, webcam2Preferences);
						log.info("webcam surveillance camera portlet added");
						 
			// Experiment Timer
			 
			log.info("adding timer portlet");
			String experimentTimerPortletId = layoutTypePortlet.addPortletId(
					Long.parseLong(request.getRemoteUser()),
					"GLORIAExperimentTimer_WAR_GLORIAExperimentTimerportlet",
					"column-3", -1);
			
			Properties timerPreferences = new Properties();
			timerPreferences.put("reservationId",String.valueOf(reservationId));
			configurePortlet(themeDisplay.getCompanyId(),
					 PortletKeys.PREFS_OWNER_ID_DEFAULT,
					 PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
					 lay.getPlid(), experimentTimerPortletId, timerPreferences);
			
			log.info("timer portlet added");
			
			
			//Add webcam in continuos mode
			
			log.info("adding CCD continuos camera portlet");
			 
			//webcam in continuous mode
			 String ccdPortletId = layoutTypePortlet.addPortletId(
			Long.parseLong(request.getRemoteUser()),
			"GLORIACCDCamera_WAR_GLORIACCDCameraportlet",
			"column-2", -1);
			 
			 Properties ccdPreferences = new Properties();
			 ccdPreferences.put("showBrightness","1");
			 ccdPreferences.put("showGain","1");
			 ccdPreferences.put("showExposure","1");
			 ccdPreferences.put("showContrast","1");
			 ccdPreferences.put("showTakeImage","1");
			 ccdPreferences.put("showContinousMode","0");
			 ccdPreferences.put("configured", "true");
			 ccdPreferences.put("reservationId",String.valueOf(reservationId));
			 ccdPreferences.put("operation","load_cont_ccd");
			 ccdPreferences.put("operation_parameter","ccd_cont_url");
			 

			 configurePortlet(themeDisplay.getCompanyId(),
			 PortletKeys.PREFS_OWNER_ID_DEFAULT,
			 PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
			 lay.getPlid(), ccdPortletId, ccdPreferences);
			
			 log.info("webcam ccd continuos camera portlet added");
			
			 // Add Mount portlet
			log.info("adding mount portlet");
			//Mount potlet
			String mountPortletId = layoutTypePortlet.addPortletId(
			Long.parseLong(request.getRemoteUser()),
			"GLORIAMount_WAR_GLORIAMountportlet", "column-3",
			-1);

			 
			 log.info("Id="+mountPortletId);
			
			 
			 Properties mountPreferences = new Properties();
			 mountPreferences.put("checkboxSpeedPanel", "0");
			 mountPreferences.put("checkboxPointerPanel", "1");
			 mountPreferences.put("checkboxRaDecPanel", "0");
			 mountPreferences.put("checkboxEpochPanel", "0");
			 mountPreferences.put("checkboxObjectPanel", "0");
			 mountPreferences.put("checkboxModePanel", "0");
			 mountPreferences.put("checkboxInformationPanel", "1");
			 mountPreferences.put("configured", "true");
			 mountPreferences.put("reservationId",
			 String.valueOf(reservationId));
			 
			 configurePortlet(themeDisplay.getCompanyId(),
						PortletKeys.PREFS_OWNER_ID_DEFAULT,
						PortletKeys.PREFS_OWNER_TYPE_LAYOUT, lay.getPlid(),
						mountPortletId, mountPreferences);

			 log.info("mount portlet added");

			 // Add focuser
			 log.info("adding focuser portlet");

			 String focuserPortletId = layoutTypePortlet.addPortletId(
			 Long.parseLong(request.getRemoteUser()),
			 "GLORIAFocuser_WAR_GLORIAFocuserportlet", "column-3",
			 -1);
			  
			 log.info("Id="+focuserPortletId);
			 Properties focuserPreferences = new Properties();
			 focuserPreferences.put("reservationId",
			 String.valueOf(reservationId));
			  
			 configurePortlet(themeDisplay.getCompanyId(),
			 PortletKeys.PREFS_OWNER_ID_DEFAULT,
			 PortletKeys.PREFS_OWNER_TYPE_LAYOUT, lay.getPlid(),
			 focuserPortletId, focuserPreferences);

			 log.info("focuser portlet added");
			 
			//add weather station portlet
			 log.info("adding weather station portlet");
			 String weatherStationId = layoutTypePortlet.addPortletId(
			 Long.parseLong(request.getRemoteUser()),
			 "GLORIAWeatherStation_WAR_GLORIAWeatherStationportlet", "column-3", -1);
			  
			 Properties weatherStationPreferences = new Properties();
			 weatherStationPreferences.put("reservationId",String.valueOf(reservationId));
			  

			 configurePortlet(themeDisplay.getCompanyId(),
			 PortletKeys.PREFS_OWNER_ID_DEFAULT,
			 PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
			 lay.getPlid(), weatherStationId, weatherStationPreferences);
			 //
			 log.info("weather station portlet added");	 


			// configurePortlet(themeDisplay.getCompanyId(),
			// PortletKeys.PREFS_OWNER_ID_DEFAULT,
			// PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
			// lay.getPlid(), mountPortletId, mountPreferences);

			// --------------------------------------------------------------------------

			LayoutLocalServiceUtil.updateLayout(lay.getGroupId(),
					lay.isPrivateLayout(), lay.getLayoutId(),
					lay.getTypeSettings());
		} catch (PortalException e) {
			log.error("Error to add layout:" + e.getMessage());
			e.printStackTrace();
		} catch (SystemException e) {
			log.error("Error to add layout:" + e.getMessage());
		}

		return url+friendlyURL;

	}
	private void configurePortlet(long companyId, long ownerId, int ownerType,
			long plid, String portletId, Properties properties) {

		try {

			PortletPreferences prefs = PortletPreferencesLocalServiceUtil
					.getPreferences(companyId, ownerId, ownerType, plid,
							portletId);
			Enumeration<Object> propertiesList = properties.keys();

			while (propertiesList.hasMoreElements()) {
				String propertyName = (String) propertiesList.nextElement();
				prefs.setValue(propertyName,
						properties.getProperty(propertyName));
			}

			PortletPreferencesLocalServiceUtil.updatePreferences(ownerId,
					ownerType, plid, portletId, prefs);

		} catch (SystemException e) {
			log.error("Error to configure portlet (system):" + e.getMessage());
		} catch (ReadOnlyException e) {
			log.error("Error to configure portlet (readonly):" + e.getMessage());
		}

	}

}
