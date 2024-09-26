package com.example.webbanhang.controller;

import com.example.webbanhang.dtos.OrderDTO;
import com.example.webbanhang.responses.OrderResponse;
import com.example.webbanhang.services.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    @PostMapping("")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result){
        try{
            if (result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{user_id}") //Thêm biến đường dẫn "user_id"
    //GET http://localhost:8088/api/v1/orders/4
    public ResponseEntity<?> getOrder(@Valid @RequestBody OrderDTO orderDTO){
        try {
            return ResponseEntity.ok("Lấy ra danh sách order từ user_id");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    //GET http://localhost:8088/api/v1/orders/2
    //Công việc của admin
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable long id,
            @Valid @RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok("Cập nhật thông tin 1 order");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@Valid @PathVariable long id){
        //Xoá mềm => cập nhật active = false
        return ResponseEntity.ok("Xoá order thành công");
    }
}