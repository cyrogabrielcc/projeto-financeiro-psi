package cef.financial.domain.dto;

import java.time.OffsetDateTime;

public class CustomerResponseDTO {

    public Long id;
    public String perfil;
    public OffsetDateTime criadoEm;

    // Construtor vazio para o Jackson
    public CustomerResponseDTO() {}

    public CustomerResponseDTO(Long id, String perfil, OffsetDateTime criadoEm) {
        this.id = id;
        this.perfil = perfil;
        this.criadoEm = criadoEm;
    }
}
