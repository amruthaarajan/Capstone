package com.amruthaa.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public final class UserInput implements Serializable{

    private static final long serialVersionUID = 1L;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("ticketId")
    private String ticketId;

    @JsonProperty("model")
    private String model;

    @JsonProperty("dataUrl")
    private String dataUrl;
}
