package com.apixenglish.center.modules.room.repository;

import com.apixenglish.center.modules.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByCampusIdAndCodeAndDeletedAtIsNull(UUID campusId, String code);

    boolean existsByCampusIdAndCodeAndDeletedAtIsNull(UUID campusId, String code);

    List<Room> findByCampusIdAndDeletedAtIsNull(UUID campusId);

    @Query("SELECT r FROM Room r " +
           "WHERE r.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(r.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(r.code) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Room> searchRooms(@Param("search") String search, Pageable pageable);
}
