package com.easydates.easydateap.repository;

import com.easydates.easydateap.model.Plan;
import com.easydates.easydateap.model.EstadoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {

    List<Plan> findByEstadoOrderByPrecioAsc(EstadoPlan estado);

    @Query("SELECT p FROM Plan p WHERE p.estado = 'ACTIVO'")
    List<Plan> findPlanesActivos();
}