package com.cts.training.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.training.model.LoanApplication;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {

	
}
