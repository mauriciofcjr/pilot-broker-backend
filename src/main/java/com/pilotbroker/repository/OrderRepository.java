package com.pilotbroker.repository;

import com.pilotbroker.model.Order;
import com.pilotbroker.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUsuario(Usuario usuario);
}
