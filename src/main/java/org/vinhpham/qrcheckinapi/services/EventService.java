package org.vinhpham.qrcheckinapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.repositories.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

}
