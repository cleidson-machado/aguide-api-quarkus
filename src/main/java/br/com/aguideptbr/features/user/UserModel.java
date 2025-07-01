package br.com.aguideptbr.features.user;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public String passwd;

    public static UserModel findByEmail(String email) {
        return find("email", email).firstResult();
    }
}