package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.DailyStats;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyStatsMapper {

    @Select("SELECT * FROM daily_stats WHERE id = #{id}")
    DailyStats findById(@Param("id") Long id);

    @Select("SELECT * FROM daily_stats WHERE date = #{date}")
    DailyStats findByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM daily_stats WHERE date BETWEEN #{startDate} AND #{endDate} ORDER BY date ASC")
    List<DailyStats> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM daily_stats ORDER BY date DESC LIMIT #{limit}")
    List<DailyStats> findRecent(@Param("limit") Integer limit);

    @Insert("INSERT INTO daily_stats(date, total_pv, total_uv, created_at) " +
            "VALUES(#{date}, #{totalPv}, #{totalUv}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyStats dailyStats);

    @Update("UPDATE daily_stats SET total_pv = #{totalPv}, total_uv = #{totalUv} WHERE id = #{id}")
    int update(DailyStats dailyStats);

    @Delete("DELETE FROM daily_stats WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Insert("INSERT INTO daily_stats (date, total_pv, total_uv, created_at) VALUES (#{date}, #{totalPv}, #{totalUv}, #{createdAt}) ON CONFLICT (date) DO UPDATE SET total_pv = #{totalPv}, total_uv = #{totalUv}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(DailyStats dailyStats);
}