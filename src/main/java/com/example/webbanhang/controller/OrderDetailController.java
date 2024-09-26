package com.example.webbanhang.controller;


import com.example.webbanhang.dtos.OrderDetailDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController {
    //Thêm mới 1 order detail
    @PostMapping
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO newOrderDetail){
        return ResponseEntity.ok("createOrderDetail here");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id){
        return ResponseEntity.ok("getOrderDetail with id = " + id);
    }

    //Lấy ra danh sách các chi tiết đơn hàng từ đơn hàng nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@Valid @PathVariable("orderId") Long orderId){
        return ResponseEntity.ok("getOrderDetails with orderId = " + orderId);
    }

    //Sửa đổi order detail nào đó
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO newOrderDetailData){
        return ResponseEntity.ok("updateOrderDetail with id = " + id + ", newOrderDetailData: " + newOrderDetailData);
    }

    //Xoá order detail nào đó
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(
            @Valid @PathVariable("id") Long id){
        return ResponseEntity.noContent().build();
    }
}

