package com.aliyara.keynotservice.services;

import com.aliyara.keynotservice.dtos.KeynoteRequestDTO;
import com.aliyara.keynotservice.dtos.KeynoteResponseDTO;

import java.util.List;

public interface KeynoteService {
    KeynoteResponseDTO saveKeynote(KeynoteRequestDTO keynoteRequestDTO);
    KeynoteResponseDTO updateKeynote(Long id, KeynoteRequestDTO keynoteRequestDTO);
    void deleteKeynote(Long id);
    List<KeynoteResponseDTO> getAllKeynotes();
    KeynoteResponseDTO getKeynoteById(Long id);
}
