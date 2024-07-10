package com.eshop.payment.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonSerialize
public class EmailFormatDto implements Serializable {
    private String toEmail;
    private String fromEmail;
    private String subject;
    private String content;
}
