package com.pkb.sandbox.pubsub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {
    private String correlationId;
    private String message;
}
