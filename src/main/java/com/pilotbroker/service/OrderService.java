package com.pilotbroker.service;

import com.pilotbroker.messaging.OrderProducer;
import com.pilotbroker.model.Order;
import com.pilotbroker.model.OrderStatus;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.OrderRepository;
import com.pilotbroker.web.dto.trade.OrderListItemDto;
import com.pilotbroker.web.dto.trade.OrderRequestDto;
import com.pilotbroker.web.dto.trade.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer   orderProducer;
    private final UsuarioService  usuarioService;

    @Transactional
    public OrderResponseDto criarOrdem(String username, OrderRequestDto dto) {
        Usuario usuario = usuarioService.buscarPorUsername(username);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setSymbol(dto.getSymbol().toUpperCase());
        order.setTipo(dto.getTipo().toUpperCase());
        order.setQuantidade(dto.getQuantidade());
        order.setPreco(dto.getPreco());
        order.setStatus(OrderStatus.PENDING);
        order.setDataCriacao(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        orderProducer.publicar(saved.getId());
        log.info("Ordem id={} criada e publicada na fila", saved.getId());

        return new OrderResponseDto(saved.getId(), "PENDING", "Ordem recebida e em processamento");
    }

    @Transactional(readOnly = true)
    public List<OrderListItemDto> listarOrdens(String username) {
        Usuario usuario = usuarioService.buscarPorUsername(username);
        return orderRepository.findAllByUsuario(usuario)
            .stream()
            .map(this::toListItemDto)
            .toList();
    }

    private OrderListItemDto toListItemDto(Order o) {
        return new OrderListItemDto(
            o.getId(), o.getSymbol(), o.getTipo(),
            o.getQuantidade(), o.getPreco(),
            o.getStatus().name(),
            o.getDataCriacao().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
