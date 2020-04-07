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

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Table(name = "PROJECTS")
@Entity
public class Project implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NaturalId
    @Column
    private String name;
    @Column
    private String commonDataTableName;
    @Column
    private String emailTemplate;
    @Column
    private String createDate;
}
