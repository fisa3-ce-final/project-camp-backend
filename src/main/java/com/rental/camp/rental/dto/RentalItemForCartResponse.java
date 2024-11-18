package com.rental.camp.rental.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalItemForCartResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private RentalItemCategory category;
    private RentalItemStatus status;
    private List<ImageDto> image;

    public RentalItemForCartResponse(Long id, String name, BigDecimal price,
                                     Integer stock, RentalItemCategory category, RentalItemStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ImageDto {
        private String imageUrl;
        private Integer imageOrder;
    }
}