/*
  @author david
 */

package com.dgc.dm.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommonDto<T> implements Serializable {

    private T rowId;

}
