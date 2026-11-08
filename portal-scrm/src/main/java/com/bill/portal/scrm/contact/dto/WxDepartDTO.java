package com.bill.portal.scrm.contact.dto;

import lombok.Data;

@Data
public class WxDepartDTO {
    private Long id;
    private String name;
    private String enName;
    private String[] departmentLeader;
    private Long parentId;
    private Long order;
}
