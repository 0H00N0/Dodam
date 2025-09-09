package com.dodam.admin.board;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;   
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Profile("admin")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<NoticeEntity> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<NoticeEntity> findById(Long id) {
        return noticeRepository.findById(id);
    }

    public List<NoticeEntity> findActiveNotices() {
        return noticeRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public NoticeEntity save(NoticeEntity notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteById(Long id) {
        noticeRepository.deleteById(id);
    }

    @Transactional
    public void delete(NoticeEntity notice) {
        noticeRepository.delete(notice);
    }

    // DTO 변환 메서드들 (API 컨트롤러용)
    public List<NoticeDTO> findAllNotices() {
        return findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        NoticeEntity entity = convertToEntity(noticeDTO);
        NoticeEntity savedEntity = noticeRepository.save(entity);
        return convertToDTO(savedEntity);
    }

    private NoticeDTO convertToDTO(NoticeEntity entity) {
        return NoticeDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .author(entity.getAuthor())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private NoticeEntity convertToEntity(NoticeDTO dto) {
        return NoticeEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .isActive(dto.getIsActive())
                .build();
    }
    public List<NoticeDTO> latest(int limit) {
    	  Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
          Page<NoticeEntity> page = noticeRepository.findAll(pageable);

        return page.getContent().stream()
                .map(this::toDto)  
                .toList();
    }
    private NoticeDTO toDto(NoticeEntity entity) {  
        if (entity == null) return null;
        NoticeDTO dto = new NoticeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}