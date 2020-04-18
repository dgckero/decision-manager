/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public interface ProcessExcelController extends HandlerExceptionResolver {
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    ModelAndView uploadFile(@RequestParam("projectName") String projectName, @RequestParam("file") MultipartFile file);
}
