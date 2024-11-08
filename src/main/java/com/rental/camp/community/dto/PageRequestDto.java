package com.rental.camp.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDto {

    private Integer page;
    private Integer size;
}