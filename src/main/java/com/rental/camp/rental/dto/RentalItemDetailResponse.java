package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RentalItemDetailResponse {
    private UUID uuid;
    private String nickname;
    private String imageUrl;
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private RentalItemCategory category;
    private RentalItemStatus status;
    private Integer viewCount;
    private BigDecimal ratingAvg;
    private Integer reviewNum;
    private LocalDateTime createdAt;
    private List<ImageDto> image;

    @Data
    public static class ImageDto {
        private String imageUrl;
        private Integer imageOrder;
    }
}
