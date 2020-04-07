/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto implements Serializable {
    private Integer id;
    private String name;
    private String commonDataTableName;
    private String emailTemplate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date createDate;
}
