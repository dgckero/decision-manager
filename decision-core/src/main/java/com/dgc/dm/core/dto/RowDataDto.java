/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
public class RowDataDto<T> extends AuditableDto<T> implements Serializable {
    private T rowId;
    private ProjectDto project;
}
