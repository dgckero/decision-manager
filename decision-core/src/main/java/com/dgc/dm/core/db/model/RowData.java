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
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
@Table(name = "Data")
@Entity
public class RowData extends Auditable<String> implements Serializable {
    private static final long serialVersionUID = -142742773753488072L;
    @Id
    @Column(name = "rowId", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rowId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT", nullable = false)
    private Project project;
}
