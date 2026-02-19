package com.example.receiver.repository;

import com.example.common.model.SharedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<SharedMessage, String> {

    @Query("SELECT m.id FROM SharedMessage m WHERE m.status = :status")
    List<String> findIdsByStatus(@Param("status") String status);

    List<SharedMessage> findByStatus(String status);
}
