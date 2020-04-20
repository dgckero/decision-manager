/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Table(name = "FILTERS")
@Entity
public class Filter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NaturalId
    @Column
    private String name;
    @Column(name = "class")
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
