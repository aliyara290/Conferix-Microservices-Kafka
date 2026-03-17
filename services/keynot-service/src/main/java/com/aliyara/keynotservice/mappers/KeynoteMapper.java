package com.aliyara.keynotservice.mappers;

import com.aliyara.keynotservice.dtos.KeynoteRequestDTO;
import com.aliyara.keynotservice.dtos.KeynoteResponseDTO;
import com.aliyara.keynotservice.entities.Keynote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeynoteMapper {
    KeynoteResponseDTO fromKeynote(Keynote keynote);
    Keynote toKeynote(KeynoteRequestDTO keynoteRequestDTO);
}