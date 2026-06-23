package com.zencube.registry.talent.dto.request;

import com.zencube.registry.profile.enums.ProjectType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TalentSearchRequest {

    private String projectDomain;
    
    private ProjectType minimumProjectType;
    
    private List<Integer> graduationYears;
    
    private String discipline;
    
    private String institution;
    
    private List<String> skills;
    
    private Boolean qualifiedOnly;

}
