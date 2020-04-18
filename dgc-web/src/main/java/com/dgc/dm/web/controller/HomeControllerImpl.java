/*
  @author david
 */
package com.dgc.dm.web.controller;

import com.dgc.dm.web.controller.iface.HomeController;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeControllerImpl extends CommonController implements HomeController {
    public ModelAndView home() {
        return new ModelAndView(CommonController.HOME_VIEW);
    }
}
