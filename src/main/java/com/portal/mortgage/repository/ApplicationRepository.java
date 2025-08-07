package com.portal.mortgage.repository;

import com.portal.mortgage.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApplicationRepository extends JpaRepository<Application, String>, JpaSpecificationExecutor<Application> {
}
