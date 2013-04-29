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

package eu.gloriaproject.tools.experiments.offline;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller("offlineExperimentsController")
@RequestMapping("VIEW")
public class OfflineExperimentsController {
	
private static final Log log = LogFactory.getLog(OfflineExperimentsController.class);

	@RequestMapping(value="VIEW")
	public ModelAndView showInit(RenderRequest renderRequest, RenderResponse response) {
		log.info("Running showInit OfflineExperiments");
		
		Map<String, Object> model = new HashMap<String, Object>();
		return new ModelAndView("offlineexperiments/view", model);
	}

}
