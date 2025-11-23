package com.tradesoncall.backend.service.search;

import com.tradesoncall.backend.model.dto.request.ServiceSearchRequest;
import com.tradesoncall.backend.model.dto.response.SearchResultsResponse;
import com.tradesoncall.backend.model.dto.response.ServiceSearchResponse;
import com.tradesoncall.backend.model.entity.SearchHistory;
import com.tradesoncall.backend.repository.SearchHistoryRepository;
import com.tradesoncall.backend.service.external.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final GooglePlacesService googlePlacesService;
    private final SearchHistoryRepository searchHistoryRepository;

    private static final int METERS_PER_MILE = 1609;

    @Transactional
    public SearchResultsResponse searchServices(UUID userId, ServiceSearchRequest request) {
        log.info("Searching for {} near {} for user {}",
                request.getServiceType(), request.getLocation(), userId);

        // Convert miles to meters for Google API
        int radiusMeters = request.getRadiusMiles() * METERS_PER_MILE;

        // Build search query
        String query = request.getServiceType().getSearchQuery();

        // Search using Google Places
        List<ServiceSearchResponse> results = googlePlacesService.searchNearby(
                query,
                request.getLocation(),
                radiusMeters,
                request.getMaxResults()
        );

        // Filter by rating if specified
        if (request.getMinRating() != null) {
            results = results.stream()
                    .filter(r -> r.getRating() != null && r.getRating() >= request.getMinRating())
                    .collect(Collectors.toList());
        }

        // Filter by open now if specified
        if (request.getOpenNow() != null && request.getOpenNow()) {
            results = results.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getOpenNow()))
                    .collect(Collectors.toList());
        }

        // Save search history
        if (!results.isEmpty()) {
            ServiceSearchResponse firstResult = results.get(0);
            saveSearchHistory(userId, request, results.size(),
                    firstResult.getLatitude(), firstResult.getLongitude());
        }

        // Build response
        return SearchResultsResponse.builder()
                .location(request.getLocation())
                .serviceType(request.getServiceType().getDisplayName())
                .totalResults(results.size())
                .results(results)
                .searchCenter(results.isEmpty() ? null :
                        SearchResultsResponse.LocationCoordinates.builder()
                                .latitude(results.get(0).getLatitude())
                                .longitude(results.get(0).getLongitude())
                                .build())
                .build();
    }

    private void saveSearchHistory(
            UUID userId,
            ServiceSearchRequest request,
            int resultsCount,
            Double latitude,
            Double longitude
    ) {
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .serviceType(request.getServiceType())
                .location(request.getLocation())
                .latitude(latitude != null ? BigDecimal.valueOf(latitude) : null)
                .longitude(longitude != null ? BigDecimal.valueOf(longitude) : null)
                .resultsCount(resultsCount)
                .build();

        searchHistoryRepository.save(history);
    }
}