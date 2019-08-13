package com.dartsBot.dartsBot.mainBot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Player2 {
    private String name;
    private Integer points;
    private Integer score;
}
