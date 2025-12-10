package org.iclassq.model.dto.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T data;
    private Boolean success;
    private String message;
}
