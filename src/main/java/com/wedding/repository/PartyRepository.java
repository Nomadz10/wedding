package com.wedding.repository;

import com.wedding.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findAllByOrderByNameAsc();
}
