package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankCard> cards = new ArrayList<>();

    public <E> User(long l, String mail, String password, HashSet<E> es) {

    }

    // Геттер для карт
    public List<BankCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    // Связные методы для управления картами
    public void addCard(BankCard card) {
        cards.add(card);
        card.setOwner(this);
    }

    public void removeCard(BankCard card) {
        cards.remove(card);
        card.setOwner(null);
    }
}
