package com.github.slamdev.springmultipledatasources.pgsvc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

interface IdentityRepository extends JpaRepository<IdentityEntity, String> {
}
