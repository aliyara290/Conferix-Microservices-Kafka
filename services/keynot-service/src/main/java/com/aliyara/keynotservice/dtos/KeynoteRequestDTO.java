package com.aliyara.keynotservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KeynoteRequestDTO {
    private String nom;
    private String prenom;
    private String email;
    private String fonction;
}
