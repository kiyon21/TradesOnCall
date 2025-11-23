package com.tradesoncall.backend.service.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tradesoncall.backend.exception.ExternalServiceException;
import com.tradesoncall.backend.model.dto.response.ServiceSearchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlacesService {

    @Value("${google.places.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    private static final String PLACES_BASE_URL = "https://places.googleapis.com/v1";
    private static final String GEOCODING_BASE_URL = "https://maps.googleapis.com/maps/api";

    // Supported place types for Nearby Search
    private static final Set<String> SUPPORTED_NEARBY_TYPES = Set.of(
            "plumber",
            "electrician",
            "roofing_contractor",
            "general_contractor",
            "locksmith",
            "painter",
            "moving_company",
            "pest_control_service"
    );

    /**
     * Search for service providers near a location
     */
    public List<ServiceSearchResponse> searchNearby(
            String serviceType,  // e.g., "plumber", "electrician", "hvac"
            String location,
            Integer radiusMeters,
            Integer maxResults
    ) {
        try {
            // Step 1: Geocode the location to get coordinates
            GeocodingResponse geocoding = geocodeLocation(location);

            if (geocoding == null || geocoding.getResults().isEmpty()) {
                throw new ExternalServiceException("Could not find location: " + location);
            }

            double lat = geocoding.getResults().get(0).getGeometry().getLocation().getLat();
            double lng = geocoding.getResults().get(0).getGeometry().getLocation().getLng();

            // Step 2: Search for places using appropriate method
            List<Place> places;
            if (SUPPORTED_NEARBY_TYPES.contains(serviceType.toLowerCase())) {
                places = searchNearbyPlaces(serviceType, lat, lng, radiusMeters, maxResults);
            } else {
                places = searchTextPlaces(serviceType, lat, lng, radiusMeters, maxResults);
            }

            if (places == null || places.isEmpty()) {
                return List.of();
            }

            // Step 3: Convert to our DTO
            return places.stream()
                    .map(place -> convertToServiceResponse(place, lat, lng))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching Google Places", e);
            throw new ExternalServiceException("Failed to search for services: " + e.getMessage());
        }
    }

    /**
     * Geocode a location string to coordinates
     */
    private GeocodingResponse geocodeLocation(String location) {
        WebClient webClient = webClientBuilder
                .baseUrl(GEOCODING_BASE_URL)
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geocode/json")
                        .queryParam("address", location)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GeocodingResponse.class)
                .block();
    }

    /**
     * Search using Nearby Search (New) API - for supported types
     */
    private List<Place> searchNearbyPlaces(
            String placeType,
            double lat,
            double lng,
            Integer radiusMeters,
            Integer maxResults
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl(PLACES_BASE_URL)
                .build();

        NearbySearchRequest request = NearbySearchRequest.builder()
                .includedTypes(List.of(placeType))
                .maxResultCount(maxResults != null ? maxResults : 20)
                .locationRestriction(LocationRestriction.builder()
                        .circle(Circle.builder()
                                .center(Center.builder()
                                        .latitude(lat)
                                        .longitude(lng)
                                        .build())
                                .radius(radiusMeters != null ? radiusMeters.doubleValue() : 8000.0)
                                .build())
                        .build())
                .rankPreference("DISTANCE")
                .build();

        PlacesSearchResponse response = webClient.post()
                .uri("/places:searchNearby")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", getFieldMask())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PlacesSearchResponse.class)
                .onErrorResume(e -> {
                    log.error("Error calling Nearby Search API", e);
                    return Mono.empty();
                })
                .block();

        return response != null ? response.getPlaces() : List.of();
    }

    /**
     * Search using Text Search API - for unsupported types like HVAC
     */
    private List<Place> searchTextPlaces(
            String query,
            double lat,
            double lng,
            Integer radiusMeters,
            Integer maxResults
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl(PLACES_BASE_URL)
                .build();

        TextSearchRequest request = TextSearchRequest.builder()
                .textQuery(query + " service")
                .maxResultCount(maxResults != null ? maxResults : 20)
                .locationBias(LocationBias.builder()
                        .circle(Circle.builder()
                                .center(Center.builder()
                                        .latitude(lat)
                                        .longitude(lng)
                                        .build())
                                .radius(radiusMeters != null ? radiusMeters.doubleValue() : 8000.0)
                                .build())
                        .build())
                .build();

        PlacesSearchResponse response = webClient.post()
                .uri("/places:searchText")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", getFieldMask())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PlacesSearchResponse.class)
                .onErrorResume(e -> {
                    log.error("Error calling Text Search API", e);
                    return Mono.empty();
                })
                .block();

        return response != null ? response.getPlaces() : List.of();
    }

    /**
     * Define which fields to return from the API
     */
    private String getFieldMask() {
        return "places.displayName," +
                "places.formattedAddress," +
                "places.location," +
                "places.rating," +
                "places.userRatingCount," +
                "places.nationalPhoneNumber," +
                "places.websiteUri," +
                "places.regularOpeningHours," +
                "places.currentOpeningHours," +
                "places.businessStatus," +
                "places.types," +
                "places.priceLevel," +
                "places.photos," +
                "places.id";
    }

    /**
     * Convert Google Place (New API) to our DTO
     */
    private ServiceSearchResponse convertToServiceResponse(
            Place place,
            double searchLat,
            double searchLng
    ) {
        // Calculate distance
        double distanceMiles = 0.0;
        if (place.getLocation() != null) {
            distanceMiles = calculateDistance(
                    searchLat, searchLng,
                    place.getLocation().getLatitude(),
                    place.getLocation().getLongitude()
            );
        }

        // Extract business name
        String businessName = place.getDisplayName() != null
                ? place.getDisplayName().getText()
                : "Unknown";

        // Check if currently open
        Boolean openNow = null;
        if (place.getCurrentOpeningHours() != null) {
            openNow = place.getCurrentOpeningHours().getOpenNow();
        }

        // Get photo URLs
        List<String> photoUrls = place.getPhotos() != null
                ? place.getPhotos().stream()
                .limit(1)
                .map(photo -> buildPhotoUrl(photo.getName()))
                .collect(Collectors.toList())
                : List.of();

        return ServiceSearchResponse.builder()
                .name(businessName)
                .address(place.getFormattedAddress())
                .phoneNumber(place.getNationalPhoneNumber())
                .rating(place.getRating())
                .totalReviews(place.getUserRatingCount())
                .priceLevel(place.getPriceLevel())
                .openNow(openNow)
                .distanceMiles(distanceMiles)
                .website(place.getWebsiteUri())
                .googleMapsUrl(buildGoogleMapsUrl(place))
                .serviceTypes(place.getTypes())
                .latitude(place.getLocation() != null ? place.getLocation().getLatitude() : null)
                .longitude(place.getLocation() != null ? place.getLocation().getLongitude() : null)
                .placeId(extractPlaceId(place.getId()))
                .photoUrls(photoUrls)
                .build();
    }

    /**
     * Build Google Maps URL from place
     */
    private String buildGoogleMapsUrl(Place place) {
        if (place.getId() != null) {
            String placeId = extractPlaceId(place.getId());
            return "https://www.google.com/maps/place/?q=place_id:" + placeId;
        }
        return null;
    }

    /**
     * Extract place ID from resource name (format: "places/ChIJ...")
     */
    private String extractPlaceId(String resourceName) {
        if (resourceName != null && resourceName.startsWith("places/")) {
            return resourceName.substring(7);
        }
        return resourceName;
    }

    /**
     * Build photo URL from photo resource name
     */
    private String buildPhotoUrl(String photoResourceName) {
        return String.format(
                "https://places.googleapis.com/v1/%s/media?maxHeightPx=400&maxWidthPx=400&key=%s",
                photoResourceName,
                apiKey
        );
    }

    /**
     * Calculate distance between two coordinates in miles (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 3959; // Radius of the earth in miles

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in miles
    }

    // ===== Request DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class NearbySearchRequest {
        private List<String> includedTypes;
        private Integer maxResultCount;
        private LocationRestriction locationRestriction;
        private String rankPreference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TextSearchRequest {
        private String textQuery;
        private Integer maxResultCount;
        private LocationBias locationBias;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LocationRestriction {
        private Circle circle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LocationBias {
        private Circle circle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Circle {
        private Center center;
        private Double radius;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Center {
        private Double latitude;
        private Double longitude;
    }

    // ===== Response DTOs =====

    @Data
    private static class GeocodingResponse {
        private List<GeocodingResult> results;
    }

    @Data
    private static class GeocodingResult {
        private Geometry geometry;
    }

    @Data
    private static class Geometry {
        private Location location;
    }

    @Data
    private static class Location {
        private Double lat;
        private Double lng;
    }

    @Data
    private static class PlacesSearchResponse {
        private List<Place> places;
    }

    @Data
    private static class Place {
        private String id;  // Resource name: "places/ChIJ..."
        private DisplayName displayName;
        private String formattedAddress;
        private PlaceLocation location;
        private Double rating;
        private Integer userRatingCount;
        private String nationalPhoneNumber;
        private String websiteUri;
        private String businessStatus;
        private Integer priceLevel;
        private List<String> types;
        private CurrentOpeningHours currentOpeningHours;
        private List<Photo> photos;
    }

    @Data
    private static class DisplayName {
        private String text;
        private String languageCode;
    }

    @Data
    private static class PlaceLocation {
        private Double latitude;
        private Double longitude;
    }

    @Data
    private static class CurrentOpeningHours {
        private Boolean openNow;
    }

    @Data
    private static class Photo {
        private String name;  // Resource name for photo
        private Integer widthPx;
        private Integer heightPx;
    }
}