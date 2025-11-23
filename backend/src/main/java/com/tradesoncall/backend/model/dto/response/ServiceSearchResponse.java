package com.tradesoncall.backend.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Service provider search result")
public class ServiceSearchResponse {

    @Schema(description = "Business name", example = "Joe's Plumbing")
    private String name;

    @Schema(description = "Full address")
    private String address;

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Average rating (1-5)", example = "4.5")
    private Double rating;

    @Schema(description = "Total number of reviews", example = "127")
    private Integer totalReviews;

    @Schema(description = "Price level (1-4, $ to $$$$)", example = "2")
    private Integer priceLevel;

    @Schema(description = "Distance from search location in miles", example = "2.3")
    private Double distanceMiles;

    @Schema(description = "Whether business is currently open")
    private Boolean openNow;

    @Schema(description = "Business website URL")
    private String website;

    @Schema(description = "Google Maps URL")
    private String googleMapsUrl;

    @Schema(description = "Types of services offered")
    private List<String> serviceTypes;

    @Schema(description = "Latitude coordinate")
    private Double latitude;

    @Schema(description = "Longitude coordinate")
    private Double longitude;

    @Schema(description = "Google Place ID")
    private String placeId;

    @Schema(description = "Photo URLs")
    private List<String> photoUrls;
}