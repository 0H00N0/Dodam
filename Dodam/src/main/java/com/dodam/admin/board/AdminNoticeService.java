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
import com.dodam.admin.board.AdminNoticeDTO;
import com.dodam.admin.board.AdminNoticeEntity;
import com.dodam.admin.board.AdminNoticeRepository;

@Service
@Profile("admin")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeService {

    private final AdminNoticeRepository noticeRepository;

    public List<AdminNoticeEntity> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<AdminNoticeEntity> findById(Long id) {
        return noticeRepository.findById(id);
    }

    public List<AdminNoticeEntity> findActiveNotices() {
        return noticeRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public AdminNoticeEntity save(AdminNoticeEntity notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteById(Long id) {
        noticeRepository.deleteById(id);
    }

    @Transactional
    public void delete(AdminNoticeEntity notice) {
        noticeRepository.delete(notice);
    }

    // DTO 변환 메서드들 (API 컨트롤러용)
    public List<AdminNoticeDTO> findAllNotices() {
        return findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminNoticeDTO createNotice(AdminNoticeDTO noticeDTO) {
    	AdminNoticeEntity entity = convertToEntity(noticeDTO);
    	AdminNoticeEntity savedEntity = noticeRepository.save(entity);
        return convertToDTO(savedEntity);
    }

    private AdminNoticeDTO convertToDTO(AdminNoticeEntity entity) {
        return AdminNoticeDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .author(entity.getAuthor())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AdminNoticeEntity convertToEntity(AdminNoticeDTO dto) {
        return AdminNoticeEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .isActive(dto.getIsActive())
                .build();
    }
    public List<AdminNoticeDTO> latest(int limit) {
    	  Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
          Page<AdminNoticeEntity> page = noticeRepository.findAll(pageable);

        return page.getContent().stream()
                .map(this::toDto)  
                .toList();
    }
    private AdminNoticeDTO toDto(AdminNoticeEntity entity) {  
        if (entity == null) return null;
        AdminNoticeDTO dto = new AdminNoticeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}