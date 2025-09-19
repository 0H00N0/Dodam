package com.dodam.delivery.repository;

import com.dodam.delivery.entity.DeliverymanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliverymanRepository extends
        JpaRepository<DeliverymanEntity, Long>,
        JpaSpecificationExecutor<DeliverymanEntity> {
}
