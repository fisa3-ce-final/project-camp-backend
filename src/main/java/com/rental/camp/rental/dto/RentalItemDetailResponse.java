package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RentalItemDetailResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String status;
    private Integer viewCount;
    private BigDecimal ratingAvg;
    private LocalDateTime createdAt;
    private List<ImageDto> image;

    @Data
    public static class ImageDto {
        private String imageUrl;
        private Integer imageOrder;
    }
}
