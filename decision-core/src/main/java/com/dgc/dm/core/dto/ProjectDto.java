/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
public class ProjectDto extends AuditableDto<String> {
    private static final long serialVersionUID = -2351483953617918420L;
    private Integer id;
    private String name;
    private String rowDataTableName;
    private String emailTemplate;
    @ToString.Exclude
    private byte[] dmnFile;
}
