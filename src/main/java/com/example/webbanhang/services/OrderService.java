package com.example.webbanhang.services;


import com.example.webbanhang.dtos.OrderDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Order;
import com.example.webbanhang.models.OrderStatus;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.OrderRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {
        Order order = new Order();
        //tìm xem user id có tồn tại không
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Không thể tìm thấy user với id: " + orderDTO.getUserId()));

        //convert orderDTO => Order
        //Sử dụng thư viện Model Mapper
        //Tạo một luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        //Cập nhật các trường của đơn hàng từ orderDTO
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(new Date());//Lấy thời điểm hiện tại
        order.setStatus(OrderStatus.PENDING);
        //Kiểm tra shipping date phải >= ngày hm nay
        Date shippingDate = orderDTO.getShippingDate();
        if (shippingDate == null || shippingDate.before(new Date())){
            throw new DataNotFoundException("Thời gian phải lớn hơn hoặc bằng hôm nay!");
        }
        order.setActive(true);
        orderRepository.save(order);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        return null;
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) {
        return null;
    }

    @Override
    public void deleteOrder(Long id) {

    }

    @Override
    public List<OrderResponse> getAllOrders(Long userId) {
        return null;
    }
}
