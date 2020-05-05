/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RowDataDto<T> implements Serializable {

    private T rowId;
    private ProjectDto project;
}