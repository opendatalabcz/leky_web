DROP INDEX IF EXISTS idx_erecept_prescription_medicinal_product_year_month;
DROP INDEX IF EXISTS idx_erecept_prescription_district_code;
DROP INDEX IF EXISTS idx_erecept_dispense_medicinal_product_year_month;
DROP INDEX IF EXISTS idx_erecept_dispense_district_code;

CREATE INDEX idx_erecept_prescription_optimized
    ON erecept_prescription (medicinal_product_id, district_code, year, month)
    INCLUDE (quantity);

CREATE INDEX idx_erecept_dispense_optimized
    ON erecept_dispense (medicinal_product_id, district_code, year, month)
    INCLUDE (quantity);

ANALYZE erecept_prescription;
ANALYZE erecept_dispense;