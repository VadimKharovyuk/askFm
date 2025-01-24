package com.example.askfm.repository;

import com.example.askfm.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportTicketRepository  extends JpaRepository<SupportTicket, Long> {

}
