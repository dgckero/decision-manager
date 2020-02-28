/**
 * @author david
 */

package com.dgc.dm.core.db.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "commonDatas")
@Entity
public class CommonEntity {
    @Id
    private int rowId;
}
