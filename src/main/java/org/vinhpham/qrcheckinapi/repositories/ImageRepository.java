package org.vinhpham.qrcheckinapi.repositories;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vinhpham.qrcheckinapi.entities.Image;

import java.util.Date;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.id IN :ids")
    List<Image> findByIdIn(@Param("ids") List<Integer> ids);

    @Query("SELECT i FROM Image i WHERE i.status = :status AND i.createdAt < :createdAt")
    List<Image> findImagesByStatusAndCreatedAt(@Param("status") boolean status, @Param("createdAt") Date createdAt);

    Image findByIdAndExtensionAndStatus(Long id, @Size(max = 10) @NotNull String extension, @NotNull Boolean status);
}
