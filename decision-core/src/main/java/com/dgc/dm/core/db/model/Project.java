/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
@DynamicUpdate
@Table(name = "PROJECTS")
@Entity
public class Project extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NaturalId
    @Column(unique = true, updatable = false)
    private String name;
    @Column(updatable = false)
    private String rowDataTableName;
    @Column
    private String emailTemplate;
    @ToString.Exclude
    @Column
    private byte[] dmnFile;
}
