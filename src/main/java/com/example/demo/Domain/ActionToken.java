package com.example.demo.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
public class ActionToken {
    @Id
    @Column(name = "token", nullable = false,length = 1024)
    private String token;


    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private UserInfo user;

    String methodName;

    @Column(columnDefinition = "TEXT")
    List<String> methodArgTypes;

    @Column(columnDefinition = "TEXT")
    String data;

    boolean deleteAfterUse;
    String dataType;

    @Past
    Date createdAt;

}
