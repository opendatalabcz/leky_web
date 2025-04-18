CREATE OR REPLACE VIEW v_medicinal_product_grouped_by_reg_number AS
SELECT
    registration_number,
    ARRAY_AGG(DISTINCT sukl_code) AS sukl_codes,
    ARRAY_AGG(DISTINCT name) AS names,
    ARRAY_AGG(DISTINCT strength) AS strengths,
    ARRAY_AGG(DISTINCT dosage_form_id) AS dosage_form_ids,
    ARRAY_AGG(DISTINCT administration_route_id) AS administration_route_ids,
    ARRAY_AGG(DISTINCT atc_group_id) AS atc_group_ids
FROM mpd_medicinal_product
WHERE registration_number IS NOT NULL AND registration_number <> ''
GROUP BY registration_number;
