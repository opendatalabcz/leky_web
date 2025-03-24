ALTER TABLE mpd_active_substance
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_active_substance
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_active_substance
    ALTER COLUMN name_inn DROP NOT NULL,
ALTER COLUMN name_en DROP NOT NULL,
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_addiction_category
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_addiction_category
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_addiction_category
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_administration_route
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_administration_route
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_administration_route
    ALTER COLUMN name DROP NOT NULL,
ALTER COLUMN name_en DROP NOT NULL,
    ALTER COLUMN name_lat DROP NOT NULL;

ALTER TABLE mpd_atc_group
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_atc_group
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_atc_group
    ALTER COLUMN type DROP NOT NULL,
ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN name_en DROP NOT NULL;

ALTER TABLE mpd_composition_flag
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_composition_flag
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_composition_flag
    ALTER COLUMN meaning DROP NOT NULL;

ALTER TABLE mpd_country
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_country
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_country
    ALTER COLUMN name DROP NOT NULL,
ALTER COLUMN name_en DROP NOT NULL,
    ALTER COLUMN edqm_code DROP NOT NULL;

ALTER TABLE mpd_dispense_type
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_dispense_type
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_dispense_type
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_doping_category
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_doping_category
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_doping_category
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_government_regulation_category
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_government_regulation_category
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_government_regulation_category
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_indication_group
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_indication_group
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_indication_group
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_measurement_unit
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_measurement_unit
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_measurement_unit
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_package_type
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_package_type
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_package_type
    ALTER COLUMN name DROP NOT NULL,
ALTER COLUMN name_en DROP NOT NULL;

ALTER TABLE mpd_registration_process
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_registration_process
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_registration_process
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_registration_status
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_registration_status
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_registration_status
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_source
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_source
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_source
    ALTER COLUMN name DROP NOT NULL;

ALTER TABLE mpd_organisation
    ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN is_manufacturer DROP NOT NULL,
    ALTER COLUMN is_marketing_authorization_holder DROP NOT NULL;

ALTER TABLE mpd_dosage_form
    ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN name_en DROP NOT NULL,
    ALTER COLUMN name_lat DROP NOT NULL,
    ALTER COLUMN is_cannabis DROP NOT NULL;