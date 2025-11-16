package cef.financial.domain.dto;


public class InvestmentProductResponseDTO {
    public Long id;
    public String nome;
    public String tipo;
    public Double rentabilidade;
    public String risco;

    public static InvestmentProductResponseDTO from(cef.financial.domain.model.InvestmentProduct p) {
        InvestmentProductResponseDTO dto = new InvestmentProductResponseDTO();
        dto.id = p.id;
        dto.nome = p.nome;
        dto.tipo = p.tipo;
        dto.rentabilidade = p.rentabilidadeAnual; // mapeia o nome
        dto.risco = p.risco;
        return dto;
    }
}