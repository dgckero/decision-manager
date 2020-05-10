/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
@DynamicUpdate
@Table(name = "FILTERS")
@Entity
public class Filter extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NaturalId
    @Column(updatable = true)
    private String name;
    @Column(name = "class", updatable = false)
    private String filterClass;
    @Column
    private String value;
    @Column
    private Boolean active;
    @Column
    private Boolean contactFilter;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT", nullable = false)
    private Project project;
}
