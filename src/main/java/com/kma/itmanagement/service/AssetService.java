package com.kma.itmanagement.service;

import com.kma.itmanagement.model.Asset;
import com.kma.itmanagement.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    public Asset saveAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    public long getAssetCount() {
        return assetRepository.count();
    }

    public long getActiveAssetCount() {
        return assetRepository.findByStatus("Active").size();
    }

    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid asset Id:" + id));
    }

    public void updateAsset(Long id, Asset updatedAsset) {
    Asset existingAsset = getAssetById(id);
    existingAsset.setName(updatedAsset.getName());
    existingAsset.setType(updatedAsset.getType());
    existingAsset.setAssetTag(updatedAsset.getAssetTag());
    existingAsset.setStatus(updatedAsset.getStatus());
    existingAsset.setLocation(updatedAsset.getLocation());
    existingAsset.setPurchaseDate(updatedAsset.getPurchaseDate());
    assetRepository.save(existingAsset);
}

    public void deleteAssetById(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new IllegalArgumentException("Invalid asset Id:" + id);
        }
        assetRepository.deleteById(id);
    }
}