package com.example.webbanhang.services;

import com.example.webbanhang.dtos.OrderDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Order;
import com.example.webbanhang.models.OrderDetail;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO) throws Exception;
    Order getOrder(Long id);
    Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;
    void deleteOrder(Long id);
    List<Order> findByUserId(Long userId);
}
