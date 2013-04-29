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

package eu.gloriaproject.tools.reservationlist;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

import eu.gloria.gs.services.experiment.online.OnlineExperimentException;
import eu.gloria.gs.services.experiment.online.data.NoSuchExperimentException;
import eu.gloria.gs.services.experiment.online.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.online.operations.NoSuchOperationException;
import eu.gloria.gs.services.experiment.online.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentReservationArgumentException;
import eu.gloria.gs.services.experiment.online.reservation.MaxReservationTimeException;
import eu.gloria.gs.services.experiment.online.reservation.NoReservationsAvailableException;
import eu.gloria.gs.services.experiment.online.reservation.NoSuchReservationException;
import eu.gloria.presentation.liferay.reservations.common.Slot;
import eu.gloria.presentation.liferay.reservations.services.ReservationsService;
import eu.gloria.presentation.liferay.reservations.services.exceptions.ConnectionException;

@Controller("reservationListController")
@RequestMapping("VIEW")
public class ReservationListController {

	private static final Log log = LogFactory.getLog(ReservationListController.class);
	
	@Resource(name = "reservationsService")
	private ReservationsService reservationsService;
	
	public void setReservationsService(ReservationsService reservationsService) {
		this.reservationsService = reservationsService;
	}
	
	@RequestMapping(value="VIEW")
	public ModelAndView showInit(RenderRequest renderRequest, RenderResponse renderResponse) {
	
        log.info("Ejecutando showInit...");

        ResourceBundle rb = ResourceBundle.getBundle("Language", renderRequest.getLocale());
        
        ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        String experiment = renderRequest.getParameter("experiment");
        
        List<String> experimentsList;
			try {
				experimentsList = reservationsService.getAllOnlineExperiments(emailUser, passwordUser);
		        // Set the list of experiments as an attribute
		        Map<String, Object> model = new HashMap<String, Object>();
		        model.put("experimentsList", experimentsList);
		        model.put("experimentsListSize", experimentsList.size());
		        
		        return new ModelAndView("reservationslist/view", model);
			} catch (OnlineExperimentException e) {
				e.printStackTrace();
				return null;
			} catch (ConnectionException e) {
				e.printStackTrace();
				return null;
			} catch (ExperimentOperationException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchOperationException e) {
				e.printStackTrace();
				return null;
			} catch (ExperimentParameterException e) {
				e.printStackTrace();
				return null;
			} catch (ExperimentNotInstantiatedException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchReservationException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchExperimentException e) {
				e.printStackTrace();
				return null;
			}
	}
	
	@RequestMapping(value="VIEW",  params="operation=reserve")
	public void makeReservation(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletModeException {

            ResourceBundle rb = ResourceBundle.getBundle("Language", actionRequest.getLocale());
            
            String beginTimeStr = actionRequest.getParameter("beginTime");
            String endTimeStr = actionRequest.getParameter("endTime");
            
            ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
            String emailUser = themeDisplay.getUser().getEmailAddress();
            String passwordUser = themeDisplay.getUser().getPassword();
            if (log.isInfoEnabled()) {
            	log.info("Making reservation from = " + beginTimeStr + " to " + endTimeStr + " for user " + emailUser);
            }
                
            // Parser from string to date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date beginTime;
            Date endTime;

            // Call to the Reservations Service
            		try {
	            		try {
							beginTime = dateFormat.parse(beginTimeStr);
							endTime = dateFormat.parse(endTimeStr);
							
							System.out.println("beginTime: " + beginTime);
							
							reservationsService.makeReservation(emailUser, passwordUser, beginTime, endTime);
		            		actionResponse.setRenderParameter("success", "true");
		            		actionResponse.setRenderParameter("message", rb.getString("msg-success-reservation"));    
						} catch (OnlineExperimentException e) {
			            	log.error("OnlineExperimentError");
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (NoReservationsAvailableException e) {
							log.error("Not slots available");
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error-not-slots"));   
						} catch (ExperimentReservationArgumentException e) {
			            	log.error("Argument Error");
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error-invalid-format"));
						} catch (MaxReservationTimeException e) {
							log.error("Max Reservation Time");
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-max-num-reservations"));   
						} catch (ConnectionException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (ExperimentOperationException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (NoSuchOperationException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (ExperimentParameterException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (ExperimentNotInstantiatedException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (NoSuchReservationException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						} catch (NoSuchExperimentException e) {
			            	log.error(e.getMessage());
			            	actionResponse.setRenderParameter("success", "false");
			            	actionResponse.setRenderParameter("message", rb.getString("msg-error"));
						}      
					} catch (ParseException e) {
						log.error("Invalid date");
	            		actionResponse.setRenderParameter("success", "false");
	            		actionResponse.setRenderParameter("message", rb.getString("msg-error-invalid-format"));
					}
                
            actionResponse.setPortletMode(PortletMode.VIEW);
	}	
	
	@RequestMapping(value="VIEW", params="operation=getTelescopes")
	public ModelAndView getTelescopes (RenderRequest renderRequest, RenderResponse renderResponse) {
			log.debug("Executing render viewing the experiments");
			
			String experiment = renderRequest.getParameter("experiment");
			
			System.out.println("Experiment: " + experiment);
            
            // Set the list of experiments as an attribute
			ModelAndView modelAndView = showInit(renderRequest, renderResponse);
            
            // If it is OK, then set a parameter 
            modelAndView.getModel().put("experimentsTaken", "true");
            
            ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
            String emailUser = themeDisplay.getUser().getEmailAddress();
            String passwordUser = themeDisplay.getUser().getPassword();

            List<String> telescopesList;
    		try {
				telescopesList = reservationsService.getAllTelescopesByExperiment(emailUser, passwordUser, experiment);
	    	    // Set the list of experiments as an attribute
	    	    modelAndView.getModel().put("telescopesList", telescopesList);
			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (ExperimentOperationException e) {
				e.printStackTrace();
			} catch (NoSuchOperationException e) {
				e.printStackTrace();
			} catch (ExperimentParameterException e) {
				e.printStackTrace();
			} catch (ExperimentNotInstantiatedException e) {
				e.printStackTrace();
			} catch (OnlineExperimentException e) {
				e.printStackTrace();
			} catch (NoSuchReservationException e) {
				e.printStackTrace();
			} catch (NoSuchExperimentException e) {
				e.printStackTrace();
			}
    	        
    	    return modelAndView;
          
	}
	
	@RequestMapping(value="VIEW", params="operation=getReservations")
	public ModelAndView getReservations (RenderRequest renderRequest, RenderResponse renderResponse) {
			log.debug("Executing render get Reservations");
			
			String telescope = renderRequest.getParameter("telescope");
			
			System.out.println("Telescope: " + telescope);
            
            // Set the list of experiments as an attribute
			ModelAndView modelAndView = getTelescopes(renderRequest, renderResponse);
            
            // If it is OK, then set a parameter 
            modelAndView.getModel().put("telescopesTaken", "true");
            
            ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
            String emailUser = themeDisplay.getUser().getEmailAddress();
            String passwordUser = themeDisplay.getUser().getPassword();

            // Get the list of reservations from the Service
            List<Slot> timeSlots;
            
                try {
					timeSlots = reservationsService.getReservationsList(emailUser, passwordUser);
	                // Set the list of reservations as an attribute
	                modelAndView.getModel().put("timeSlots", timeSlots);
	                return modelAndView;
				} catch (OnlineExperimentException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (ExperimentReservationArgumentException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (ConnectionException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (ExperimentOperationException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (NoSuchOperationException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (ExperimentParameterException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (ExperimentNotInstantiatedException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (NoSuchReservationException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				} catch (NoSuchExperimentException e) {
	                log.error(e.getMessage());
	                modelAndView.getModel().put("errorMessage",  e.getMessage());
	                return modelAndView;
				}
            
	}

	@ResourceMapping(value="getExperiments")
    public void getExperiments(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        log.info("Ejecutando getExperiments...");

        ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
        List<String> experimentsList;
			try {
				experimentsList = reservationsService.getAllOnlineExperiments(emailUser, passwordUser);
				
				List<Experiment> experiments = new ArrayList<Experiment>();
				int i = 0;
				for (String experimentStr : experimentsList) {
					Experiment experiment = new Experiment(new Long(i++), experimentStr);
					experiments.add(experiment);
				}
				
			    Map<String, Object> results = new HashMap<String, Object>();
			    results.put("experiments", experiments);
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (OnlineExperimentException e) {
				e.printStackTrace();
			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (ExperimentOperationException e) {
				e.printStackTrace();
			} catch (NoSuchOperationException e) {
				e.printStackTrace();
			} catch (ExperimentParameterException e) {
				e.printStackTrace();
			} catch (ExperimentNotInstantiatedException e) {
				e.printStackTrace();
			} catch (NoSuchReservationException e) {
				e.printStackTrace();
			} catch (NoSuchExperimentException e) {
				e.printStackTrace();
			}
	}
	
	@ResourceMapping(value="getTelescopes")
    public void getTelescopes(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        
        String experiment = request.getParameter("experimentSelectedName");
        
        log.info("Running getTelescopes in the experiment: " + experiment); 

        ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
        List<String> telescopesList;
			try {
				telescopesList = reservationsService.getAllTelescopesByExperiment(emailUser, passwordUser, experiment);
				
				List<Telescope> telescopes = new ArrayList<Telescope>();
				int i = 0;
				for (String telescopeStr : telescopesList) {
					Telescope telescope = new Telescope(new Long(i++), telescopeStr);
					telescopes.add(telescope);
				}
				
			    Map<String, Object> results = new HashMap<String, Object>();
			    results.put("telescopes", telescopes);
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);				
			} catch (OnlineExperimentException e) {
				e.printStackTrace();
			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (ExperimentOperationException e) {
				e.printStackTrace();
			} catch (NoSuchOperationException e) {
				e.printStackTrace();
			} catch (ExperimentParameterException e) {
				e.printStackTrace();
			} catch (ExperimentNotInstantiatedException e) {
				e.printStackTrace();
			} catch (NoSuchReservationException e) {
				e.printStackTrace();
			} catch (NoSuchExperimentException e) {
				e.printStackTrace();
			}
	}
	
	@ResourceMapping(value="getSlots")
    public void getSlots (ResourceRequest request, ResourceResponse response) throws IOException {
		
		String telescope = request.getParameter("telescopeSelectedName");
		
		System.out.println("Telescopio: " + telescope);
		
		ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
		// Get the list of slots from the Service
        List<Slot> timeSlots;
            try {
            	// TODO To set as an argument telescope and experiment
				timeSlots = reservationsService.getReservationsList(emailUser, passwordUser);
				
			    Map<String, Object> results = new HashMap<String, Object>();
			    results.put("slots", timeSlots);
			    GsonBuilder gsonBuilder = new GsonBuilder();
			    gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
	        	String json = gsonBuilder.create().toJson(results);
			    
	        	response.setContentType("application/json");
	        	response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (OnlineExperimentException e) {
                log.error(e.getMessage());
            	log.error("There is no timeslots available");
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error-not-slots"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ExperimentReservationArgumentException e) {
                log.error(e.getMessage());
			} catch (ConnectionException e) {
                log.error(e.getMessage());
			} catch (ExperimentOperationException e) {
                log.error(e.getMessage());
			} catch (NoSuchOperationException e) {
                log.error(e.getMessage());
			} catch (ExperimentParameterException e) {
                log.error(e.getMessage());
			} catch (ExperimentNotInstantiatedException e) {
                log.error(e.getMessage());
			} catch (NoSuchReservationException e) {
                log.error(e.getMessage());
			} catch (NoSuchExperimentException e) {
                log.error(e.getMessage());
			}
        
	}
	
	@ResourceMapping(value="doReserve")
    public void doReserve (ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		log.info("Running doReserve ...");
		
		String begin = request.getParameter("beginDateSelected");
		String end = request.getParameter("endDateSelected");
        
        ResourceBundle rb = ResourceBundle.getBundle("Language", request.getLocale());
        
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String emailUser = themeDisplay.getUser().getEmailAddress();
        String passwordUser = themeDisplay.getUser().getPassword();
        
        // Parser from string to date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginTime;
        Date endTime;
        
        try {
    		try {
				beginTime = dateFormat.parse(begin);
				endTime = dateFormat.parse(end);
				
				reservationsService.makeReservation(emailUser, passwordUser, beginTime, endTime);
				
			    Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "true");
			    results.put("message", rb.getString("msg-success-reservation"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (OnlineExperimentException e) {
            	log.error("OnlineExperimentError");
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (NoReservationsAvailableException e) {
				log.error("Not slots available");
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error-slot-reserved"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ExperimentReservationArgumentException e) {
            	log.error("Argument Error");
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error-invalid-format"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (MaxReservationTimeException e) {
				log.error("Max Reservation Time");
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-max-num-reservations"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ConnectionException e) {
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ExperimentOperationException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (NoSuchOperationException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ExperimentParameterException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (ExperimentNotInstantiatedException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (NoSuchReservationException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} catch (NoSuchExperimentException e) {
				e.printStackTrace();
            	log.error(e.getMessage());
            	Map<String, Object> results = new HashMap<String, Object>();
			    results.put("success", "false");
			    results.put("message", rb.getString("msg-error"));
	        	String json = new Gson().toJson(results);
			    
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    Writer writer = response.getWriter();
			    writer.write(json);
			} 
		} catch (ParseException e) {
			log.error("Invalid date");	
			Map<String, Object> results = new HashMap<String, Object>();
		    results.put("success", "false");
		    results.put("message", rb.getString("msg-error-invalid-format"));
        	String json = new Gson().toJson(results);
		    
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    Writer writer = response.getWriter();
		    writer.write(json);
		}
				 
	}

}
