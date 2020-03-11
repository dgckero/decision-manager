/**
 * @author david
 */

package com.dgc.dm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class FilterCreationDto {
    @NonNull
    private List<FilterDto> filters;
}
