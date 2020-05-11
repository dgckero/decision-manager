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
public class RowDataDto<T> extends AuditableDto<T> {
    private T rowId;
    private ProjectDto project;
}
