/*
  @author david
 */

package com.dgc.dm.core.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

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
    @Column(unique = true, updatable = true)
    private String name;
    @Column(updatable = true)
    private String rowDataTableName;
    @Column
    private String emailTemplate;
    @ToString.Exclude
    @Column
    private byte[] dmnFile;
}
