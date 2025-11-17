package cef.financial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;


@Entity
@Table(name = "HTE_CUSTOMER")
public class Customer extends PanacheEntityBase {

    @Id
    // IMPORTANTE: sem @GeneratedValue aqui, o ID será definido pela aplicação
    public Long id;


    @Column(name = "PERFIL", length = 30)
    public String perfil; // Conservador / Moderado / Agressivo (se quiser persistir)

    @Column(name = "CRIADO_EM")
    public OffsetDateTime criadoEm;

    public Customer() {}


}
