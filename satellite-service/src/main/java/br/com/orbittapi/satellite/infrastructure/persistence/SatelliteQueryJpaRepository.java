package br.com.orbittapi.satellite.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SatelliteQueryJpaRepository extends JpaRepository<SatelliteQueryJpaEntity, UUID> {
}
