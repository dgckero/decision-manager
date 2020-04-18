/*
  @author david
 */

package com.dgc.dm.core.service.db;

import lombok.AccessLevel;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
public class CommonServer {
    @Autowired
    private ModelMapper modelMapper;
}
