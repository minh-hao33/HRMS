package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum RequestTypeEnum {
    PAID_LEAVE("Paid leave"),
    UNPAID_LEAVE("Unpaid leave"),
    SICK_LEAVE("Sick leave with social insurance benefits"),
    MATERNITY_LEAVE("Maternity leave – caring for child after birth"),
    PATERNITY_LEAVE("Paternity leave - caring for wife after birth"),
    COMPENSATORY_LEAVE("Compensatory leave"),
    STUDY_LEAVE("Study leave"),
    PERSONAL_LEAVE("Personal leave"),
    BEREAVEMENT_LEAVE("Bereavement leave"),
    PREGNANCY_EXAM_LEAVE("Pregnancy examination leave"),
    CONTRACEPTIVE_MEASURE_LEAVE("Contraceptive measure leave"),
    MISCARRIAGE_LEAVE("Miscarriage leave, stillbirth"),
    FAMILY_WEDDING_FUNERAL_LEAVE("Unpaid leave – family member's wedding/funeral"),
    EMPLOYEE_WEDDING_LEAVE("Paid leave - employee's wedding"),
    CHILD_WEDDING_LEAVE("Paid leave – child's wedding"),
    FAMILY_DEATH_LEAVE("Paid leave – family member's death");

    private final String value;

    RequestTypeEnum(String value) {
        this.value = value;
    }
}
