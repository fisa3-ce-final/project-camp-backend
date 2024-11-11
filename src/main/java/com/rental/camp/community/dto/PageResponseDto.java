package com.rental.camp.community.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PageResponseDto {

    private List<CommunityPostResponseDto> content;
    private int totalPages;
    private long totalElements;
    private int numberOfElements;
    private boolean last;
    private boolean first;
    private int size;
    private int number;

    public PageResponseDto(List<CommunityPostResponseDto> content, Page<?> page) {
        this.content = content;
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.numberOfElements = page.getNumberOfElements();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.size = page.getSize();
        this.number = page.getNumber();
    }
}
