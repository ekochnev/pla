package com.pla.sharedkernel.identifier;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author: pradyumna
 * @since 1.0 11/03/2015
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlanId implements Serializable {

    @Column(name = "plan_id")
    private String planId;

    public String toString() {
        return planId;
    }
}
