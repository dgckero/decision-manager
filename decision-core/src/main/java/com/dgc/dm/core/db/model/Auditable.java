/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class Auditable<U> implements Serializable {

    @CreatedDate
    @Column(name = "dataCreationDate", updatable = false)
    private String dataCreationDate;

    @LastModifiedDate
    @Column(name = "lastUpdatedDate")
    private String lastUpdatedDate;
}
