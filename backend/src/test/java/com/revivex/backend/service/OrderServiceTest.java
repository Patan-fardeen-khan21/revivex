package com.revivex.backend.service;

import com.revivex.backend.dto.OrderDto;
import com.revivex.backend.entity.*;
import com.revivex.backend.repository.CartRepository;
import com.revivex.backend.repository.InterventionLogRepository;
import com.revivex.backend.repository.OrderRepository;
import com.revivex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterventionLogRepository interventionLogRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderFromCart_Success() {
        // Arrange
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Product product = Product.builder().id(1L).price(new BigDecimal("100.00")).build();
        CartItem cartItem = CartItem.builder().id(1L).product(product).quantity(2).build();
        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);
        Cart cart = Cart.builder().id(1L).user(user).items(items).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        // Act
        OrderDto result = orderService.createOrderFromCart(email, null);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertTrue(result.getRazorpayOrderId().startsWith("order_MOCK_"));
        assertTrue(cart.getItems().isEmpty()); // Cart should be cleared
        verify(cartRepository, times(1)).save(cart);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrderFromCart_EmptyCart() {
        // Arrange
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Cart cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(email, null);
        });

        assertEquals("Cart is empty", exception.getMessage());
    }
}
