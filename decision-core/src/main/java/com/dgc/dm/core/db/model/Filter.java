/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
@DynamicUpdate
@Table(name = "FILTERS")
@Entity
public class Filter extends Auditable<String> {
    private static final long serialVersionUID = -2240436501746172768L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NaturalId
    @Column(updatable = false)
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
