package com.aliyara.keynotservice.controller;

import com.aliyara.keynotservice.dtos.KeynoteRequestDTO;
import com.aliyara.keynotservice.dtos.KeynoteResponseDTO;
import com.aliyara.keynotservice.services.KeynoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keynotes")
@RequiredArgsConstructor
public class KeynoteRestController {

    private final KeynoteService keynoteService;

    @PostMapping
    public KeynoteResponseDTO save(@RequestBody KeynoteRequestDTO requestDTO) {
        return keynoteService.saveKeynote(requestDTO);
    }

    @GetMapping
    public List<KeynoteResponseDTO> list() {
        return keynoteService.getAllKeynotes();
    }

    @GetMapping("/{id}")
    public KeynoteResponseDTO getById(@PathVariable Long id) {
        return keynoteService.getKeynoteById(id);
    }

    @PutMapping("/{id}")
    public KeynoteResponseDTO update(@PathVariable Long id, @RequestBody KeynoteRequestDTO requestDTO) {
        return keynoteService.updateKeynote(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        keynoteService.deleteKeynote(id);
    }
}
