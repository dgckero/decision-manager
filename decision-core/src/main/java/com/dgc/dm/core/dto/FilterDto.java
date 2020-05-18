/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
public class FilterDto extends AuditableDto<String> {
    private static final long serialVersionUID = -2392676399973987679L;
    private Integer id;
    private String name;
    private String filterClass;
    private String value;
    private Boolean active;
    private Boolean contactFilter;
    private ProjectDto project;
}
