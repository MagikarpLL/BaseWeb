package com.example.personalwebsite.model.dto.request;

import lombok.Data;

@Data
public class TrackRequest {
    private String pageUrl;
    private String visitorId;
    private String referrer;
}
