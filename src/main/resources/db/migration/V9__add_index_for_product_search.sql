CREATE INDEX idx_mpd_medicinal_product_unified_search
    ON mpd_medicinal_product USING gin (
    (LOWER(name || ' ' || sukl_code || ' ' || registration_number)) gin_trgm_ops
    );

ANALYZE mpd_medicinal_product;