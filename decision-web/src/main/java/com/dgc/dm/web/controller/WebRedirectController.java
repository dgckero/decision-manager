/*
  @author david
 */
package com.dgc.dm.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class WebRedirectController extends CommonController implements com.dgc.dm.web.controller.iface.WebRedirectController {

    /**
     * Go to home view
     *
     * @return home view
     */
    @Override
    public ModelAndView home ( ) {
        return new ModelAndView(CommonController.HOME_VIEW);
    }

    /**
     * Go to newProject view
     *
     * @return newProject view
     */
    @Override
    public ModelAndView newProject ( ) {
        return new ModelAndView(CommonController.NEW_PROJECT_VIEW);
    }

    /**
     * Go to information view
     *
     * @return information view
     */
    @Override
    public ModelAndView information ( ) {
        return new ModelAndView(CommonController.INFORMATION_VIEW);
    }

}
