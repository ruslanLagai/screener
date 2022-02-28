package com.home.project.stocks.model.entity;

import com.home.project.stocks.model.telegram.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "telegram_chat")
public class TelegramChatEntity {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;
    private String firstName;
    private String lastName;
    private String userName;
    private double threshold;
}
