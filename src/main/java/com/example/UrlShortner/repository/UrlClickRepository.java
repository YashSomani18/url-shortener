package com.example.UrlShortner.repository;

import com.example.UrlShortner.dto.ClickAnalyticsDto;
import com.example.UrlShortner.model.UrlClick;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick, String> {
    
    Long countByUrlKey(String urlKey);
    
    Long countByUrlKeyAndClickedAtGreaterThanEqual(String urlKey, LocalDateTime startDate);
    
    List<UrlClick> findByUrlKeyAndClickedAtBetweenOrderByClickedAtDesc(String urlKey, 
                                                                      LocalDateTime startDate, 
                                                                      LocalDateTime endDate);

    Page<UrlClick> findByUrlKeyOrderByClickedAtDesc(String urlKey, Pageable pageable);

    @Query("SELECT DATE_TRUNC('hour', u.clickedAt) as hour, COUNT(u) as count " +
            "FROM UrlClick u WHERE u.urlKey = :urlKey AND u.clickedAt >= :since " +
            "GROUP BY DATE_TRUNC('hour', u.clickedAt) ORDER BY hour")
    List<ClickAnalyticsDto> getHourlyClickStats(@Param("urlKey") String urlKey,
                                                @Param("since") LocalDateTime since);

    @Query("SELECT DATE_TRUNC('day', u.clickedAt) as day, COUNT(u) as count " +
            "FROM UrlClick u WHERE u.urlKey = :urlKey " +
            "AND u.clickedAt BETWEEN :start AND :end " +
            "GROUP BY DATE_TRUNC('day', u.clickedAt) ORDER BY day")
    List<ClickAnalyticsDto> getDailyClickStats(@Param("urlKey") String urlKey,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
    
    // These complex aggregations still need @Query since JPA method naming can't handle GROUP BY
    @Query("SELECT c.country, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByCountry(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.browser, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey GROUP BY c.browser ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByBrowser(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.deviceType, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey GROUP BY c.deviceType ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByDeviceType(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.utmSource, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey AND c.utmSource IS NOT NULL GROUP BY c.utmSource ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByUtmSource(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.utmMedium, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey AND c.utmMedium IS NOT NULL GROUP BY c.utmMedium ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByUtmMedium(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.utmCampaign, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey AND c.utmCampaign IS NOT NULL GROUP BY c.utmCampaign ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByUtmCampaign(@Param("urlKey") String urlKey);
    
    @Query("SELECT c.operatingSystem, COUNT(c) FROM UrlClick c WHERE c.urlKey = :urlKey AND c.operatingSystem IS NOT NULL GROUP BY c.operatingSystem ORDER BY COUNT(c) DESC")
    List<Object[]> getClickStatsByOperatingSystem(@Param("urlKey") String urlKey);
} 