CREATE OR REPLACE VIEW v_medicinal_product_grouped_by_reg_number AS
SELECT
    mp.registration_number,
    ARRAY_AGG(DISTINCT mp.sukl_code) AS sukl_codes,
    ARRAY_AGG(DISTINCT mp.name) AS names,
    ARRAY_AGG(DISTINCT mp.strength) AS strengths,
    ARRAY_AGG(DISTINCT mp.dosage_form_id) AS dosage_form_ids,
    ARRAY_AGG(DISTINCT mp.administration_route_id) AS administration_route_ids,
    ARRAY_AGG(DISTINCT mp.atc_group_id) AS atc_group_ids,
    ARRAY_AGG(DISTINCT mps.substance_id) AS substance_ids
FROM
    mpd_medicinal_product mp
    LEFT JOIN
    mpd_medicinal_product_substance mps
ON mp.id = mps.medicinal_product_id
WHERE
    mp.registration_number IS NOT NULL
  AND mp.registration_number <> ''
GROUP BY
    mp.registration_number;