/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import lombok.extern.log4j.Log4j2;
import org.camunda.bpm.dmn.engine.impl.type.DateDataTypeTransformer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
class DecisionManagerDateDataTypeTransformer extends DateDataTypeTransformer {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Format date
     *
     * @param value
     * @return date parsed
     */
    @Override
    protected Date transformString(String value) {
        log.debug("[INIT] transformString parsing: {}", value);
        try {
            Date parsedDate = simpleDateFormat.parse(value);
            log.debug("[END] transformString parsedDate: {}", parsedDate);
            return parsedDate;
        } catch (ParseException e) {
            log.error("Error parsing date " + e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }
}
