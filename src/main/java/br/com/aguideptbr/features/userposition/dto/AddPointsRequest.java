package br.com.aguideptbr.features.userposition.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de adição de pontos ao ranking de usuário.
 *
 * Suporta dois campos:
 * - points: Quantidade de pontos a adicionar (obrigatório)
 * - reason: Motivo da adição de pontos (opcional, para auditoria)
 *
 * @see br.com.aguideptbr.features.userposition.enuns.PointsReason
 */
public class AddPointsRequest {

    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 1000, message = "Points must not exceed 1000 per request")
    private Integer points;

    @Size(max = 100, message = "Reason must not exceed 100 characters")
    private String reason;

    // Constructors

    public AddPointsRequest() {
    }

    public AddPointsRequest(Integer points, String reason) {
        this.points = points;
        this.reason = reason;
    }

    // Getters and Setters

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "AddPointsRequest{" +
                "points=" + points +
                ", reason='" + reason + '\'' +
                '}';
    }
}
