package com.rental.camp.rental.dto;

import lombok.Getter;

@Getter
public class RentalItemRequestDto {
    private int page; // 페이지 번호
    private int size; // 한 페이지에 표시할 항목
}
