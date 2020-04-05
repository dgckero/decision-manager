/*
  @author david
 */
package com.dgc.dm.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @RequestMapping("/")
    public ModelAndView test(final HttpServletResponse response) {
        return new ModelAndView("home");
    }
}
