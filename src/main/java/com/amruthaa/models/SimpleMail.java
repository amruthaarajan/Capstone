package com.amruthaa.models;

import lombok.Data;

@Data
public class SimpleMail {

    private String to;
    private String subject;
    private String content;
    private String attachment;

    public SimpleMail(String to,String subject, String attachment) {
        this.to = to;
        this.subject = subject;
        this.content = "Your results are ready to be viewed. Please find the results attached with this email.";
        this.attachment = attachment;
    }
}
