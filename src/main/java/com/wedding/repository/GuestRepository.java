package com.wedding.repository;

import com.wedding.model.Guest;
import com.wedding.model.RsvpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByFirstNameLowerAndLastNameLower(String firstNameLower, String lastNameLower);

    List<Guest> findAllByOrderByLastNameAscFirstNameAsc();

    long countByRsvpStatus(RsvpStatus status);
}
