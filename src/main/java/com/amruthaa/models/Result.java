package com.amruthaa.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.Data;

@Data
public final class Result implements Serializable{

    private static final long serialVersionUID = 1L;

    @JsonProperty("model")
    private String model;

    @JsonProperty("accuracy")
    private String accuracy;

    @JsonProperty("timeTaken")
    private String timeTaken;

    public Result(String model, String accuracy, String timeTaken) {
        this.accuracy = accuracy;
        this.model= model;
        this.timeTaken = timeTaken;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
