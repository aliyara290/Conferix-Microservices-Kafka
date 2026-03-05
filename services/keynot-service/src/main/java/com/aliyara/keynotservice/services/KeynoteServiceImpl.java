package com.aliyara.keynotservice.services;

import com.aliyara.keynotservice.dtos.KeynoteRequestDTO;
import com.aliyara.keynotservice.dtos.KeynoteResponseDTO;
import com.aliyara.keynotservice.entities.Keynote;
import com.aliyara.keynotservice.mappers.KeynoteMapper;
import com.aliyara.keynotservice.repositories.KeynoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class KeynoteServiceImpl implements KeynoteService {

    private final KeynoteRepository keynoteRepository;
    private final KeynoteMapper keynoteMapper;

    @Override
    public KeynoteResponseDTO saveKeynote(KeynoteRequestDTO keynoteRequestDTO) {
        Keynote keynote = keynoteMapper.toKeynote(keynoteRequestDTO);
        Keynote savedKeynote = keynoteRepository.save(keynote);
        return keynoteMapper.fromKeynote(savedKeynote);
    }

    @Override
    public KeynoteResponseDTO updateKeynote(Long id, KeynoteRequestDTO keynoteRequestDTO) {
        Keynote keynote = keynoteRepository.findById(id).orElseThrow(() -> new RuntimeException("Keynote not found"));
        keynote.setNom(keynoteRequestDTO.getNom());
        keynote.setPrenom(keynoteRequestDTO.getPrenom());
        keynote.setEmail(keynoteRequestDTO.getEmail());
        keynote.setFonction(keynoteRequestDTO.getFonction());
        Keynote updatedKeynote = keynoteRepository.save(keynote);
        return keynoteMapper.fromKeynote(updatedKeynote);
    }

    @Override
    public void deleteKeynote(Long id) {
        keynoteRepository.deleteById(id);
    }

    @Override
    public List<KeynoteResponseDTO> getAllKeynotes() {
        return keynoteRepository.findAll().stream()
                .map(keynoteMapper::fromKeynote)
                .collect(Collectors.toList());
    }

    @Override
    public KeynoteResponseDTO getKeynoteById(Long id) {
        Keynote keynote = keynoteRepository.findById(id).orElseThrow(() -> new RuntimeException("Keynote not found"));
        return keynoteMapper.fromKeynote(keynote);
    }
}
