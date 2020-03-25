/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "commonDatas")
@Entity
public class CommonEntity implements Serializable {
    @Id
    @Column(name = "rowId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rowId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT", nullable = false)
    private Project project;
}
