package com.rental.camp.global.config;

import org.springframework.context.event.EventListener;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener {

    @EventListener
    public void onSessionCreated(SessionCreatedEvent event) {
        System.out.println("Session created: " + event.getSessionId());
    }

    @EventListener
    public void onSessionDeleted(SessionDeletedEvent event) {
        System.out.println("Session deleted: " + event.getSessionId());
    }
}