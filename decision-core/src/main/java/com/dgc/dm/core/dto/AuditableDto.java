/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
public class AuditableDto<U> implements Serializable {
    private static final long serialVersionUID = 8559179058473875292L;
    private String dataCreationDate;

    private String lastUpdatedDate;
}
