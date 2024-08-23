package com.ex.data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchesDTO {
    private Integer branchId;
    private String branchesName;
    private String postCode;
    private String address;
    private String address2;
    private String phone;
    private Boolean active;
    private List<MonthcareGroupsDTO> monthcareGroups;
    private Double latitude;
    private Double longitude;
}


