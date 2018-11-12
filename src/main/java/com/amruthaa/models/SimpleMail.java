package com.amruthaa.models;

import lombok.Data;

@Data
public class SimpleMail {

    private String to;
    private String subject;
    private String content;
    private String attachment;

    public SimpleMail(String to,String subject, String content, String attachment) {
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.attachment = attachment;
    }
}
