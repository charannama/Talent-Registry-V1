package com.zencube.registry.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalDetailResponse {
    private String fieldName;
    private String oldValue;
    private String newValue;
}
