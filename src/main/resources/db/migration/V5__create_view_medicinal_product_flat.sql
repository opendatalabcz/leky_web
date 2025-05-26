CREATE OR REPLACE VIEW view_medicinal_product_flat AS
SELECT
    mp.id,
    mp.first_seen,
    mp.missing_since,
    mp.sukl_code,
    mp.reporting_obligation,
    mp.name,
    mp.strength,
    df.code AS dosage_form_code,
    mp.packaging,
    ar.code AS administration_route_code,
    mp.supplementary_information,
    pt.code AS package_type_code,
    mah.code AS mah_code,
    mah_c.code AS mah_country_code,
    curr_mah.code AS current_mah_code,
    curr_mah_c.code AS curr_mah_country_code,
    rs.code AS registration_status_code,
    mp.registration_valid_to,
    mp.registration_unlimited,
    mp.market_supply_end_date,
    ig.code AS indication_group_code,
    atc.code AS atc_group_code,
    mp.registration_number,
    mp.parallel_import_id,
    pis.code AS parallel_import_supplier_code,
    pis_c.code AS parallel_import_supplier_country_code,
    rp.code AS registration_process_code,
    mp.daily_dose_amount,
    dd_mu.code AS daily_dose_unit_code,
    mp.daily_dose_packaging,
    mp.who_source,
    mp.substance_list,
    dt.code AS dispense_type_code,
    ac.code AS addiction_category_code,
    dc.code AS doping_category_code,
    grc.code AS gov_regulation_category_code,
    mp.deliveries_flag,
    mp.ean,
    mp.braille,
    mp.expiry_period_duration,
    mp.expiry_period_unit,
    mp.registered_name,
    mp.mrp_number,
    mp.registration_legal_basis,
    mp.safety_feature,
    mp.prescription_restriction,
    mp.medicinal_product_type
FROM mpd_medicinal_product mp
LEFT JOIN mpd_dosage_form df ON mp.dosage_form_id = df.id
LEFT JOIN mpd_administration_route ar ON mp.administration_route_id = ar.id
LEFT JOIN mpd_package_type pt ON mp.package_type_id = pt.id
LEFT JOIN mpd_organisation mah ON mp.marketing_authorization_holder_id = mah.id
LEFT JOIN mpd_country mah_c ON mah.country_id = mah_c.id
LEFT JOIN mpd_organisation curr_mah ON mp.current_marketing_authorization_holder_id = curr_mah.id
LEFT JOIN mpd_country curr_mah_c ON curr_mah.country_id = curr_mah_c.id
LEFT JOIN mpd_registration_status rs ON mp.registration_status_id = rs.id
LEFT JOIN mpd_indication_group ig ON mp.indication_group_id = ig.id
LEFT JOIN mpd_atc_group atc ON mp.atc_group_id = atc.id
LEFT JOIN mpd_organisation pis ON mp.parallel_import_supplier_id = pis.id
LEFT JOIN mpd_country pis_c ON pis.country_id = pis_c.id
LEFT JOIN mpd_registration_process rp ON mp.registration_process_id = rp.id
LEFT JOIN mpd_measurement_unit dd_mu ON mp.daily_dose_unit_id = dd_mu.id
LEFT JOIN mpd_dispense_type dt ON mp.dispense_type_id = dt.id
LEFT JOIN mpd_addiction_category ac ON mp.addiction_category_id = ac.id
LEFT JOIN mpd_doping_category dc ON mp.doping_category_id = dc.id
LEFT JOIN mpd_government_regulation_category grc ON mp.government_regulation_category_id = grc.id;
