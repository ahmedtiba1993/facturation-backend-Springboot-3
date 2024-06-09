package com.facturation.repository;

import com.facturation.model.UrlFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UrlFileRepository extends JpaRepository<UrlFile, Long> {

    Optional<UrlFile> findByUuid(UUID uuid);

    @Query("select u from UrlFile u where (u.facture.id = :id or u.devis.id = :id or u.bondeLivraison.id = :id)")
    UrlFile verifExist(Long id);
}
