package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum RequestTypeEnum {
    PAID_LEAVE("Nghỉ phép có lương"),
    UNPAID_LEAVE("Nghỉ phép không lương"),
    SICK_LEAVE("Nghỉ ốm hưởng chế độ BHXH"),
    MATERNITY_LEAVE("Nghỉ thai sản – chăm sóc con sau sinh"),
    PATERNITY_LEAVE("Nghỉ thai sản - chăm sóc vợ sau sinh"),
    COMPENSATORY_LEAVE("Nghỉ bù"),
    STUDY_LEAVE("Nghỉ học"),
    PERSONAL_LEAVE("Nghỉ việc riêng"),
    BEREAVEMENT_LEAVE("Nghỉ do tang lễ"),
    PREGNANCY_EXAM_LEAVE("Nghỉ khám thai"),
    CONTRACEPTIVE_MEASURE_LEAVE("Nghỉ thực hiện biện pháp tránh thai"),
    MISCARRIAGE_LEAVE("Nghỉ sảy thai, thai chết lưu"),
    FAMILY_WEDDING_FUNERAL_LEAVE("Nghỉ không hưởng lương – người thân kết hôn/mất"),
    EMPLOYEE_WEDDING_LEAVE("Nghỉ hưởng lương - CBNV kết hôn"),
    CHILD_WEDDING_LEAVE("Nghỉ hưởng lương – Con kết hôn"),
    FAMILY_DEATH_LEAVE("Nghỉ hưởng lương – người thân mất");

    private final String value;

    RequestTypeEnum(String value) {
        this.value = value;
    }
}
