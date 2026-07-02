package com.zencube.registry.journal.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {
    private String fieldName;
    private String oldValue;
    private String newValue;
}
