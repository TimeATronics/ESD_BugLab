package edu.iiitb.esdlab3.service;

import edu.iiitb.esdlab3.dto.CreateEventRequest;
import edu.iiitb.esdlab3.entity.Event;
import edu.iiitb.esdlab3.exception.EventNotFoundException;
import edu.iiitb.esdlab3.repository.EventRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event create(CreateEventRequest request) {
        return eventRepository.save(
                new Event(
                        request.getTitle().trim(),
                        request.getCapacity()
                )
        );
    }

    public List<Event> list() {
        return eventRepository.findAll();
    }

    public Event get(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }
}