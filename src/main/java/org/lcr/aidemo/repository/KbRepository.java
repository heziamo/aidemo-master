package org.lcr.aidemo.repository;

import org.lcr.aidemo.entity.Kb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface KbRepository extends JpaRepository<Kb, Long>, JpaSpecificationExecutor<Kb> {
    Page<Kb> findByQContaining(String q, Pageable page);
    Optional<Kb> findByQ(String question);
}