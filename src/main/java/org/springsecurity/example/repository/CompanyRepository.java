package org.springsecurity.example.repository;

import org.springsecurity.example.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
