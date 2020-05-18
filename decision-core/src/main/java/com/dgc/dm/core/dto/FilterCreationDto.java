/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class FilterCreationDto implements Serializable {

    private static final long serialVersionUID = -5281911749000764925L;
    private List<FilterDto> filters;

}
