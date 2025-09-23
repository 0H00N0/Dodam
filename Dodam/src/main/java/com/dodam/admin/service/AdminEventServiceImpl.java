package com.dodam.admin.service;

import com.dodam.admin.dto.EventRequestDTO;
import com.dodam.admin.dto.EventResponseDTO;
import com.dodam.event.entity.EventNumber;
import com.dodam.event.repository.EventNumberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    private final EventNumberRepository eventNumberRepository;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO dto) {
        EventNumber event = EventNumber.builder()
                .evName(dto.getEvName())
                .evContent(dto.getEvContent())
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .eventType(dto.getEventType() != null ? dto.getEventType() : "FIRST") // 기본값: 선착순
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EventNumber saved = eventNumberRepository.save(event);
        return toDTO(saved);
    }

    @Override
    public EventResponseDTO updateEvent(Long evNum, EventRequestDTO dto) {
        EventNumber event = eventNumberRepository.findById(evNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 이벤트를 찾을 수 없습니다. ID=" + evNum));

        event.setEvName(dto.getEvName());
        event.setEvContent(dto.getEvContent());
        event.setStatus(dto.getStatus());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setEventType(dto.getEventType() != null ? dto.getEventType() : event.getEventType());
        event.setUpdatedAt(LocalDateTime.now());

        EventNumber updated = eventNumberRepository.save(event);
        return toDTO(updated);
    }


    @Override
    public void deleteEvent(Long evNum) {
        if (!eventNumberRepository.existsById(evNum)) {
            throw new EntityNotFoundException("해당 이벤트를 찾을 수 없습니다. ID=" + evNum);
        }
        eventNumberRepository.deleteById(evNum);
    }

    @Override
    public EventResponseDTO getEventById(Long evNum) {
        EventNumber event = eventNumberRepository.findById(evNum)
                .orElseThrow(() -> new EntityNotFoundException("해당 이벤트를 찾을 수 없습니다. ID=" + evNum));
        return toDTO(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventNumberRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private EventResponseDTO toDTO(EventNumber entity) {
        return EventResponseDTO.builder()
                .evNum(entity.getEvNum())
                .evName(entity.getEvName())
                .evContent(entity.getEvContent())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .eventType(entity.getEventType())
                .build();
    }
}
