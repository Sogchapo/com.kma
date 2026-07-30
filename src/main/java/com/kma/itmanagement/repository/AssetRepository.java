package com.kma.itmanagement.repository;

import com.kma.itmanagement.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    // For counting the statistics on your dashboard
    long countByStatus(String status);

    // ADD THIS LINE: For fetching lists of assets filtered by status (Active, Retired, etc.)
    List<Asset> findByStatus(String status);
}