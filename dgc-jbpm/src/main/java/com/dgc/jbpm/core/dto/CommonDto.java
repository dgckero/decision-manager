package com.dgc.jbpm.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommonDto<T> implements Serializable {

    private T rowId;

}
