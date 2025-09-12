// ScoreMetaDTO.java
package com.eos.admin.dto;

import lombok.Data;

@Data
public class ScoreMetaDTO {
    private Long batchId;
    private Integer maxAttempts;
    private Integer passingMarks;
}
	