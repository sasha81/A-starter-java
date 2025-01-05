package org.composer.core.utils;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Task<V,I,O> implements Serializable {
    private String id;
    private V initValue;
    private I input;
    private O output;
    private String errorMessage;
}
