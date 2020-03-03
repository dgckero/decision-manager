/**
 * @author david
 */

package com.dgc.dm.core.db.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "commonDatas")
@Entity
public class CommonEntity {
    @Id
    @Column(name = "rowId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rowId;
}
