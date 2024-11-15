package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalItemResponse {
    private String nickname;
    private String userImageUrl;
    private Long rentalId;
    private String rentalItemName;
    private BigDecimal price;
    private Integer stock;
    private RentalItemCategory category;
    private RentalItemStatus status;
    private BigDecimal ratingAvg;

    public RentalItemResponse(Long rentalId, String rentalItemName,
                              BigDecimal price, Integer stock,
                              RentalItemCategory category, RentalItemStatus status) {
        this.rentalId = rentalId;
        this.rentalItemName = rentalItemName;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
    }
}
