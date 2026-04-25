package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Registra bloqueios unilaterais entre usuários.
 * A bloqueia B não implica B bloqueia A.
 *
 * Tabela: app_user_block
 */
@Entity
@Table(name = "app_user_block", uniqueConstraints = @UniqueConstraint(name = "unique_user_block", columnNames = {
        "blocker_user_id", "blocked_user_id" }))
public class UserBlockModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Usuário que executou o bloqueio.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_user_id", nullable = false)
    public UserModel blocker;

    /**
     * Usuário que foi bloqueado.
     * Não pode enviar mensagens DIRECT para o bloqueador.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    public UserModel blocked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;
}
