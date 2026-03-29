package br.com.aguideptbr.features.userposition.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta de histórico de pontos.
 * Representa uma entrada individual no histórico de adição de pontos.
 */
public class PointsHistoryResponse {
    private LocalDateTime date;
    private Integer points;
    private String reason;
    private Integer totalScoreAfter;
    private String ipAddress;

    public PointsHistoryResponse() {
    }

    public PointsHistoryResponse(
            LocalDateTime date,
            Integer points,
            String reason,
            Integer totalScoreAfter,
            String ipAddress) {
        this.date = date;
        this.points = points;
        this.reason = reason;
        this.totalScoreAfter = totalScoreAfter;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

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

    public Integer getTotalScoreAfter() {
        return totalScoreAfter;
    }

    public void setTotalScoreAfter(Integer totalScoreAfter) {
        this.totalScoreAfter = totalScoreAfter;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
