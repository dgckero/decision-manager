/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto implements Serializable {
    private Integer id;
    private String name;
    private String rowDataTableName;
    private String emailTemplate;
    private String createDate;
    @ToString.Exclude
    private byte[] dmnFile;
}
