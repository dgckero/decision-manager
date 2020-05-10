/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
public class ProjectDto extends AuditableDto<String> {
    private Integer id;
    private String name;
    private String rowDataTableName;
    private String emailTemplate;
    @ToString.Exclude
    private byte[] dmnFile;
}
