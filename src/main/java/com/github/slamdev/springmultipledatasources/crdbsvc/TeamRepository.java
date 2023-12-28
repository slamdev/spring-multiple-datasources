package com.github.slamdev.springmultipledatasources.crdbsvc;

import org.springframework.data.jpa.repository.JpaRepository;

interface TeamRepository extends JpaRepository<TeamEntity, String> {
}
