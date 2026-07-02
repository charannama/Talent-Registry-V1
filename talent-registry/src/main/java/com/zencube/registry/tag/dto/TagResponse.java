package com.zencube.registry.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private UUID id;
    private String name;
    private String slug;
    private String category;
    private Instant createdAt;
}
