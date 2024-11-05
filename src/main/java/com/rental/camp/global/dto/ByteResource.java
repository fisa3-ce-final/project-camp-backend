package com.rental.camp.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ByteResource {
    private byte[] resource;
    private String contentType;
}
