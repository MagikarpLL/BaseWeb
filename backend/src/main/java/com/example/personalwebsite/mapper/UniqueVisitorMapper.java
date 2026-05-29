package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.UniqueVisitor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UniqueVisitorMapper {

    @Select("SELECT * FROM unique_visitor WHERE id = #{id}")
    UniqueVisitor findById(@Param("id") Long id);

    @Select("SELECT * FROM unique_visitor WHERE visitor_id = #{visitorId}")
    UniqueVisitor findByVisitorId(@Param("visitorId") String visitorId);

    @Select("SELECT * FROM unique_visitor ORDER BY last_visit_at DESC")
    List<UniqueVisitor> findAll();

    @Insert("INSERT INTO unique_visitor(visitor_id, first_visit_at, last_visit_at, total_pv) " +
            "VALUES(#{visitorId}, #{firstVisitAt}, #{lastVisitAt}, #{totalPv})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UniqueVisitor uniqueVisitor);

    @Update("UPDATE unique_visitor SET last_visit_at = #{lastVisitAt}, total_pv = #{totalPv} WHERE id = #{id}")
    int update(UniqueVisitor uniqueVisitor);

    @Delete("DELETE FROM unique_visitor WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM unique_visitor")
    long countTotal();

    @Select("SELECT COUNT(*) FROM unique_visitor WHERE last_visit_at >= #{since}")
    long countRecent(@Param("since") java.time.LocalDateTime since);

    @Select("SELECT COUNT(*) FROM unique_visitor WHERE DATE(last_visit_at) = #{date}")
    long countByDate(@Param("date") java.time.LocalDate date);
}