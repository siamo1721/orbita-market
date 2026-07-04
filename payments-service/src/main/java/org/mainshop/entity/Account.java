package org.mainshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "balance", nullable = false)
    @PositiveOrZero(message = "Баланс не может быть меньше 0")
    private Long balance;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

}
