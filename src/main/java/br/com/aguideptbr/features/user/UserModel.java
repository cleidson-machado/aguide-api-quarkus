package br.com.aguideptbr.features.user;
import com.fasterxml.jackson.annotation.JsonIgnore; //JENKIS TEST ONLY
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "app_user")
public class UserModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    public String name;
    public String surname;
    public String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String passwd;

    /**
     * Busca um usuário pelo email.
     * @param email Email do usuário
     * @return UserModel encontrado ou null
     */
    public static UserModel findByEmail(String email) {
        return find("email", email).firstResult();
    }
    
    /**
     * Retorna o nome completo do usuário.
     * @return Nome completo (nome + sobrenome)
     */
    public String getFullName() {
        return name + " " + surname;
    }
}