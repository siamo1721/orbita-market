package org.mainshop.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboxRepository extends JpaRepository<Inbox, Long> {

    boolean existsByEventId(UUID eventId);
}
