package com.rumoaopratico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private int totalProcessed;
    private int totalImported;
    private int totalErrors;
    private List<String> errors;
    private String topicName;
    private Long topicId;
}
