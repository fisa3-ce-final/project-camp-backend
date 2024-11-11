package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RentalItemCreateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private List<ImageDto> images;

    @Data
    public static class ImageDto {
        private String imageUrl; // TODO: MultipartFile 변경 필요
        private Integer imageOrder;
    }
}
