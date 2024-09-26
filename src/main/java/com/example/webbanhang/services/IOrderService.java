package com.example.webbanhang.services;

import com.example.webbanhang.dtos.OrderDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrder(Long id);
    OrderResponse updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
    List<OrderResponse> getAllOrders(Long userId);
}
