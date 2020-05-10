/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
public class FilterDto extends AuditableDto<String> {
    private Integer id;
    private String name;
    private String filterClass;
    private String value;
    private Boolean active;
    private Boolean contactFilter;
    private ProjectDto project;
}
