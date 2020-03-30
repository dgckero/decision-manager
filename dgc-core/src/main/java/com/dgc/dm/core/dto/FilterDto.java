/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterDto implements Serializable {
    private Integer id;
    private String name;
    private String filterClass;
    private String value;
    private Boolean active;
    private Boolean contactFilter;
    private ProjectDto project;
}
