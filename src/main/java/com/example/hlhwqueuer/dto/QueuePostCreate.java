package com.example.hlhwqueuer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueuePostCreate {
    private Integer userId;
    private String content;
}
