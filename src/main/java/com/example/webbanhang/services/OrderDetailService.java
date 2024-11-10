package com.example.webbanhang.services;

import com.example.webbanhang.dtos.OrderDetailDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Order;
import com.example.webbanhang.models.OrderDetail;
import com.example.webbanhang.models.Product;
import com.example.webbanhang.repositories.OrderDetailRepository;
import com.example.webbanhang.repositories.OrderRepository;
import com.example.webbanhang.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderDetailService implements IOrderDetailService{
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException{
        //tìm xem orderId có tồn tại không
        Order order = orderRepository.findById(orderDetailDTO.getOrderID())
                .orElseThrow(() -> new DataNotFoundException("Cannot find Order with id: " + orderDetailDTO.getOrderID()));
        //Tim product theo id
        Product product = productRepository.findById(orderDetailDTO.getProductID())
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + orderDetailDTO.getProductID()));

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .build();
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(Long id) throws DataNotFoundException{
        return orderDetailRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find OrderDetail with id: " + id));
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException{
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id: " + id));
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderID())
                .orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + id));
        Product existingProduct = productRepository.findById(orderDetailDTO.getProductID())
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + orderDetailDTO.getProductID()));
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setProduct(existingProduct);
        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
