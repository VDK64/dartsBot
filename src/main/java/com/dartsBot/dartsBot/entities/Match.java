package com.dartsBot.dartsBot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    @Id
    @Column(nullable = false, unique = true)
    private Long chatId;
    private String player1Name;
    private String player2Name;
    private Integer player1points;
    private Integer player2points;
    private Integer player1Score;
    private Integer player2Score;

    public Match(Long chatId) {
        this.chatId = chatId;
    }
}
