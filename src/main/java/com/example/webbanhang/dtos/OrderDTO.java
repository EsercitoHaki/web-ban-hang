package com.example.webbanhang.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDTO {
    @JsonProperty("user_id")
    @Min(value = 1, message = "User ID phải lớn hơn 1")
    private Long userId;

    @JsonProperty("fullname")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    @Size(min = 5, message = "Số điện thoại phải trên 5 ký tự")
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String address;

    private String note;

    @JsonProperty("total_money")
    @Min(value = 0, message = "Tổng tiền phải lớn hơn 0")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private Date shippingDate;

    @JsonProperty("payment_method")
    private String paymentMethod;

}
