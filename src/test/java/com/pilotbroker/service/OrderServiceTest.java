package com.pilotbroker.service;

import com.pilotbroker.messaging.OrderProducer;
import com.pilotbroker.model.Order;
import com.pilotbroker.model.OrderStatus;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.OrderRepository;
import com.pilotbroker.web.dto.trade.OrderListItemDto;
import com.pilotbroker.web.dto.trade.OrderRequestDto;
import com.pilotbroker.web.dto.trade.OrderResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderProducer   orderProducer;
    @Mock private UsuarioService  usuarioService;

    @InjectMocks
    private OrderService orderService;

    // ── criarOrdem ───────────────────────────────────────────────────────────

    @Test
    void criarOrdem_DeveSalvarComStatusPending_EPublicarNaFila() {
        Usuario usuario = usuarioComId(1L);
        when(usuarioService.buscarPorUsername("user@test.com")).thenReturn(usuario);

        Order saved = orderComId(10L, usuario, OrderStatus.PENDING);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderRequestDto dto = new OrderRequestDto("AAPL", "BUY", 10, 175.30);
        orderService.criarOrdem("user@test.com", dto);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        InOrder inOrder = inOrder(orderRepository, orderProducer);
        inOrder.verify(orderRepository).save(captor.capture());
        inOrder.verify(orderProducer).publicar(10L);

        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void criarOrdem_DeveNormalizarSymbolETipoParaMaiusculo() {
        Usuario usuario = usuarioComId(1L);
        when(usuarioService.buscarPorUsername("user@test.com")).thenReturn(usuario);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderRequestDto dto = new OrderRequestDto("aapl", "buy", 5, 100.0);
        orderService.criarOrdem("user@test.com", dto);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getSymbol()).isEqualTo("AAPL");
        assertThat(captor.getValue().getTipo()).isEqualTo("BUY");
    }

    @Test
    void criarOrdem_DeveRetornarOrderResponseDtoCorreto() {
        Usuario usuario = usuarioComId(1L);
        when(usuarioService.buscarPorUsername("user@test.com")).thenReturn(usuario);

        Order saved = orderComId(42L, usuario, OrderStatus.PENDING);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderRequestDto dto = new OrderRequestDto("AAPL", "BUY", 10, 175.30);
        OrderResponseDto response = orderService.criarOrdem("user@test.com", dto);

        assertThat(response.getOrderId()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMessage()).isEqualTo("Ordem recebida e em processamento");
    }

    // ── listarOrdens ─────────────────────────────────────────────────────────

    @Test
    void listarOrdens_DeveRetornarApenasOrdensDoUsuario() {
        Usuario usuario = usuarioComId(1L);
        when(usuarioService.buscarPorUsername("user@test.com")).thenReturn(usuario);

        Order o1 = orderComId(1L, usuario, OrderStatus.PENDING);
        Order o2 = orderComId(2L, usuario, OrderStatus.PROCESSED);
        when(orderRepository.findAllByUsuario(usuario)).thenReturn(List.of(o1, o2));

        List<OrderListItemDto> result = orderService.listarOrdens("user@test.com");

        assertThat(result).hasSize(2);
        verify(orderRepository).findAllByUsuario(usuario);
    }

    @Test
    void listarOrdens_DeveMapearCamposCorretamente() {
        Usuario usuario = usuarioComId(1L);
        when(usuarioService.buscarPorUsername("user@test.com")).thenReturn(usuario);

        Order order = orderComId(5L, usuario, OrderStatus.PROCESSED);
        order.setSymbol("MSFT");
        order.setTipo("SELL");
        order.setQuantidade(3);
        order.setPreco(410.50);
        order.setDataCriacao(LocalDateTime.of(2026, 3, 27, 10, 30, 0));
        when(orderRepository.findAllByUsuario(usuario)).thenReturn(List.of(order));

        List<OrderListItemDto> result = orderService.listarOrdens("user@test.com");

        OrderListItemDto item = result.get(0);
        assertThat(item.getId()).isEqualTo(5L);
        assertThat(item.getSymbol()).isEqualTo("MSFT");
        assertThat(item.getTipo()).isEqualTo("SELL");
        assertThat(item.getQuantidade()).isEqualTo(3);
        assertThat(item.getPreco()).isEqualTo(410.50);
        assertThat(item.getStatus()).isEqualTo("PROCESSED");
        assertThat(item.getDataCriacao()).isEqualTo("2026-03-27T10:30:00");
    }

    // ── OrderConsumer ─────────────────────────────────────────────────────────

    @Test
    void consumer_DeveAtualizarStatusParaProcessed() {
        com.pilotbroker.messaging.OrderConsumer consumer =
            new com.pilotbroker.messaging.OrderConsumer(orderRepository);

        Order order = orderComId(7L, usuarioComId(1L), OrderStatus.PENDING);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        consumer.processar(7L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSED);
        verify(orderRepository).save(order);
    }

    @Test
    void consumer_DeveLogarAviso_QuandoOrdemNaoEncontrada() {
        com.pilotbroker.messaging.OrderConsumer consumer =
            new com.pilotbroker.messaging.OrderConsumer(orderRepository);

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Não deve lançar exception
        consumer.processar(99L);

        verify(orderRepository, never()).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Usuario usuarioComId(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("user@test.com");
        return u;
    }

    private Order orderComId(Long id, Usuario usuario, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setUsuario(usuario);
        o.setSymbol("AAPL");
        o.setTipo("BUY");
        o.setQuantidade(10);
        o.setPreco(175.30);
        o.setStatus(status);
        o.setDataCriacao(LocalDateTime.now());
        return o;
    }
}
