package com.rest.rest.repository;

import com.example.rest.model.Rest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RestRepository extends JpaRepository<Rest, Long> {

}
