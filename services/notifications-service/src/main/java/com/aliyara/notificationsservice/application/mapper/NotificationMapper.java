package com.aliyara.notificationsservice.application.mapper;

import com.aliyara.notificationsservice.application.dto.NotificationDTO;
import com.aliyara.notificationsservice.application.dto.NotificationRequest;
import com.aliyara.notificationsservice.application.dto.NotificationResponse;
import com.aliyara.notificationsservice.domain.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {
    
    NotificationDTO toDTO(Notification notification);
    
    List<NotificationDTO> toDTOList(List<Notification> notifications);
    
    NotificationResponse toResponse(Notification notification);
    
    List<NotificationResponse> toResponseList(List<Notification> notifications);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "tentatives", ignore = true)
    @Mapping(target = "erreurMessage", ignore = true)
    @Mapping(target = "dateEnvoi", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notification toEntity(NotificationRequest request);
}
