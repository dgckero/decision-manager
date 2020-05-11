/*
  @author david
 */

package com.dgc.dm.core.db.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
@Table(name = "Data")
@Entity
public class RowData extends Auditable<String> {
    @Id
    @Column(name = "rowId", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rowId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT", nullable = false)
    private Project project;
}
