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
    private String emailTemplate;
    @DateTimeFormat(pattern = "E, d MMM yyyy HH:mm:ss Z")
    private Date createDate;
}
