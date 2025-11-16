package cef.financial.domain.dto;

import java.time.OffsetDateTime;

public class CustomerResponseDTO {

    public Long id;
    public String nome;
    public String documento;
    public String email;
    public String perfil;
    public OffsetDateTime criadoEm;

    // Construtor vazio para o Jackson
    public CustomerResponseDTO() {}

    public CustomerResponseDTO(Long id, String nome, String documento,
                               String email, String perfil, OffsetDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.documento = documento;
        this.email = email;
        this.perfil = perfil;
        this.criadoEm = criadoEm;
    }
}
