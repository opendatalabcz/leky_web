CREATE INDEX idx_erecept_prescription_medicinal_product_year_month
    ON erecept_prescription (medicinal_product_id, year, month);

CREATE INDEX idx_erecept_prescription_district_code
    ON erecept_prescription (district_code);

CREATE INDEX idx_erecept_dispense_medicinal_product_year_month
    ON erecept_dispense (medicinal_product_id, year, month);

CREATE INDEX idx_erecept_dispense_district_code
    ON erecept_dispense (district_code);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_mpd_medicinal_product_name_trigram
    ON mpd_medicinal_product USING gin (name gin_trgm_ops);

CREATE INDEX idx_mpd_medicinal_product_registration_number
    ON mpd_medicinal_product (registration_number);