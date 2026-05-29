package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.PageView;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PageViewMapper {

    @Select("SELECT * FROM page_view WHERE id = #{id}")
    PageView findById(@Param("id") Long id);

    @Select("SELECT * FROM page_view WHERE page_url = #{pageUrl} AND date = #{date} AND hour = #{hour}")
    PageView findByPageUrlAndDateAndHour(@Param("pageUrl") String pageUrl,
                                         @Param("date") LocalDate date,
                                         @Param("hour") Integer hour);

    @Select("SELECT * FROM page_view WHERE date = #{date} ORDER BY hour ASC")
    List<PageView> findByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM page_view WHERE date BETWEEN #{startDate} AND #{endDate} ORDER BY date ASC, hour ASC")
    List<PageView> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Insert("INSERT INTO page_view(page_url, referrer, pv_count, uv_count, date, hour, created_at) " +
            "VALUES(#{pageUrl}, #{referrer}, #{pvCount}, #{uvCount}, #{date}, #{hour}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PageView pageView);

    @Update("UPDATE page_view SET pv_count = #{pvCount}, uv_count = #{uvCount}, referrer = #{referrer} WHERE id = #{id}")
    int update(PageView pageView);

    @Delete("DELETE FROM page_view WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COALESCE(SUM(pv_count), 0) FROM page_view WHERE date = #{date}")
    Integer sumPvByDate(@Param("date") LocalDate date);

    @Select("SELECT COALESCE(SUM(uv_count), 0) FROM page_view WHERE date = #{date}")
    Integer sumUvByDate(@Param("date") LocalDate date);

    @Select("SELECT page_url as pageUrl, SUM(pv_count) as pvCount FROM page_view WHERE date BETWEEN #{startDate} AND #{endDate} GROUP BY page_url ORDER BY pvCount DESC LIMIT #{limit}")
    List<PageView> findTopPages(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limit") Integer limit);

    @Select("SELECT COALESCE(referrer, 'direct') as referrer, SUM(pv_count) as pvCount FROM page_view WHERE date BETWEEN #{startDate} AND #{endDate} GROUP BY referrer ORDER BY pvCount DESC LIMIT #{limit}")
    List<PageView> findTopSources(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limit") Integer limit);
}