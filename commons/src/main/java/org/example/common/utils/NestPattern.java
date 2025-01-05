package org.example.common.utils;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class NestPattern implements Serializable {
    private String cmd;
}
