package com.github.slamdev.springmultipledatasources.chsvc;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<ProductEntity, String> {
}
