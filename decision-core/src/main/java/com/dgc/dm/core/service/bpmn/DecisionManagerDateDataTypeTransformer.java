/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.impl.type.DateDataTypeTransformer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
class DecisionManagerDateDataTypeTransformer extends DateDataTypeTransformer {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Format date
     *
     * @param value
     * @return date parsed
     */
    protected Date transformString (final String value) {
        log.debug("[INIT] transformString parsing: {}", value);
        try {
            final Date parsedDate = this.format.parse(value);
            log.debug("[END] transformString parsedDate: {}", parsedDate);
            return parsedDate;
        } catch (final ParseException e) {
            log.error("Error parsing date " + e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }
}