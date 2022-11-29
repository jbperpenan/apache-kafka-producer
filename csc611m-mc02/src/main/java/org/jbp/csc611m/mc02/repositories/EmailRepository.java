package org.jbp.csc611m.mc02.repositories;


import org.jbp.csc611m.mc02.entities.Email;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends CrudRepository<Email, Long> {
}