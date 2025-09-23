package com.dodam.event.service;

import com.dodam.event.entity.EventNumber;
import com.dodam.event.entity.First;
import com.dodam.event.entity.Drawing;
import com.dodam.event.entity.LotteryTicket;
import com.dodam.event.repository.EventNumberRepository;
import com.dodam.event.repository.FirstRepository;
import com.dodam.event.repository.DrawingRepository;
import com.dodam.event.repository.LotteryTicketRepository;
import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;
import com.dodam.event.dto.EventJoinRequestDTO;
import com.dodam.event.dto.EventResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventNumberRepository eventNumberRepository;
    private final FirstRepository firstRepository;
    private final DrawingRepository drawingRepository;
    private final LotteryTicketRepository lotteryTicketRepository;
    private final MemberRepository memberRepository;
    
    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventNumberRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO getEvent(Long evNum) {
        EventNumber event = eventNumberRepository.findById(evNum)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID=" + evNum));
        return toDTO(event);
    }

    @Override
    public String joinEvent(EventJoinRequestDTO request) {
        EventNumber event = eventNumberRepository.findById(request.getEvNum())
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        MemberEntity member = memberRepository.findById(request.getMNum())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if ("FIRST".equalsIgnoreCase(event.getEventType())) {
            First first = new First();
            first.setEvent(event);
            first.setMember(member);
            first.setFDate(LocalDateTime.now());
            first.setWinState(0); // 기본 미당첨 상태
            firstRepository.save(first);

            return "선착순 이벤트 참여 완료!";
        } else if ("DRAWING".equalsIgnoreCase(event.getEventType())) {
            if (request.getLotNum() == null) {
                throw new IllegalArgumentException("추첨 이벤트에는 추첨권이 필요합니다.");
            }

            LotteryTicket ticket = lotteryTicketRepository.findById(request.getLotNum())
                    .orElseThrow(() -> new EntityNotFoundException("추첨권을 찾을 수 없습니다."));

            if (!ticket.getMember().getMnum().equals(member.getMnum())) {
                throw new IllegalStateException("해당 추첨권은 이 회원의 것이 아닙니다.");
            }

            if (ticket.getStatus() != 0) { // 0=미사용
                throw new IllegalStateException("이미 사용했거나 만료된 추첨권입니다.");
            }

            Drawing draw = new Drawing();
            draw.setEvent(event);
            draw.setMember(member);
            draw.setLotteryTicket(ticket);
            draw.setDrawState(0); // 기본 미당첨 상태
            draw.setDrawDate(LocalDateTime.now());
            drawingRepository.save(draw);

            // 추첨권 상태 변경
            ticket.setStatus(1); // 사용 처리
            lotteryTicketRepository.save(ticket);

            return "추첨 이벤트 참여 완료!";
        } else {
            throw new IllegalArgumentException("알 수 없는 이벤트 유형입니다: " + event.getEventType());
        }
    }
    private EventResponseDTO toDTO(EventNumber entity) {
        return EventResponseDTO.builder()
                .evNum(entity.getEvNum())
                .evName(entity.getEvName())
                .evContent(entity.getEvContent())
                .eventType(entity.getEventType())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .build();
    }
}
