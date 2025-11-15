package cef.financial.domain.dto;

public class RiskProfileResponse {

    public Long clienteId;
    public String perfil;  // "Conservador", "Moderado", "Agressivo"
    public int pontuacao;
    public String descricao;
}
