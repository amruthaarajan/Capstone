package com.amruthaa.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public final class UserInput implements Serializable{

    private static final long serialVersionUID = 1L;

    @JsonProperty("email")
    private String email;

    @JsonProperty("ticketId")
    private String ticketId;

    @JsonProperty("model")
    private String model;

    @JsonProperty("dataUrl")
    private String dataUrl;

    public UserInput(String email, String ticketId, String model, String dataUrl) {
        this.email = email;
        this.ticketId = ticketId;
        this.model= model;
        this.dataUrl = dataUrl;
    }
}
