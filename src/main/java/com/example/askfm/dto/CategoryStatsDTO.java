package com.example.askfm.dto;

import com.example.askfm.enums.GroupCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryStatsDTO {
    private GroupCategory category;
    private String displayName;
    private long count;
    private boolean isSelected;
    private long totalCount;
}
