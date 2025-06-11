package com.ruerpc.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rue
 * @date 2025/6/10 13:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {

    private byte code;
    private String compressType;
    private Compressor compressor;
}
