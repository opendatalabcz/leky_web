-- 1) mpd_country
CREATE TABLE IF NOT EXISTS mpd_country (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        VARCHAR(3) UNIQUE NOT NULL,
    name        TEXT,
    name_en     TEXT,
    edqm_code   VARCHAR(2)
);

-- 2) mpd_addiction_category
CREATE TABLE IF NOT EXISTS mpd_addiction_category (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 3) mpd_doping_category
CREATE TABLE IF NOT EXISTS mpd_doping_category (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 4) mpd_government_regulation_category
CREATE TABLE IF NOT EXISTS mpd_government_regulation_category (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 5) mpd_source
CREATE TABLE IF NOT EXISTS mpd_source (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 6) mpd_composition_flag
CREATE TABLE IF NOT EXISTS mpd_composition_flag (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    meaning     TEXT
);

-- 7) mpd_dispense_type
CREATE TABLE IF NOT EXISTS mpd_dispense_type (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 8) mpd_measurement_unit
CREATE TABLE IF NOT EXISTS mpd_measurement_unit (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 9) mpd_registration_process
CREATE TABLE IF NOT EXISTS mpd_registration_process (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 10) mpd_registration_status
CREATE TABLE IF NOT EXISTS mpd_registration_status (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 11) mpd_indication_group
CREATE TABLE IF NOT EXISTS mpd_indication_group (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT
);

-- 12) mpd_atc_group
CREATE TABLE IF NOT EXISTS mpd_atc_group (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    type        CHAR(1) CHECK (type IN ('N','C')),
    name        TEXT,
    name_en     TEXT
);

-- 13) mpd_package_type
CREATE TABLE IF NOT EXISTS mpd_package_type (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT,
    name_en     TEXT,
    edqm_code   BIGINT
);

-- 14) mpd_administration_route
CREATE TABLE IF NOT EXISTS mpd_administration_route (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT,
    name_en     TEXT,
    name_lat    TEXT,
    edqm_code   BIGINT
);

-- 15) mpd_dosage_form
CREATE TABLE IF NOT EXISTS mpd_dosage_form (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name        TEXT,
    name_en     TEXT,
    name_lat    TEXT,
    is_cannabis BOOLEAN,
    edqm_code   BIGINT
);

-- 16) mpd_organisation
CREATE TABLE IF NOT EXISTS mpd_organisation (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT NOT NULL,
    country_id  BIGINT NOT NULL REFERENCES mpd_country(id) ON DELETE RESTRICT,
    name        TEXT,
    is_manufacturer BOOLEAN,
    is_marketing_authorization_holder BOOLEAN,
    CONSTRAINT mpd_organisation_code_country_uq UNIQUE (code, country_id)
);

-- 17) mpd_active_substance
CREATE TABLE IF NOT EXISTS mpd_active_substance (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    name_inn    TEXT,
    name_en     TEXT,
    name        TEXT,
    addiction_category_id BIGINT REFERENCES mpd_addiction_category(id) ON DELETE RESTRICT
);

-- 18) mpd_substance
CREATE TABLE IF NOT EXISTS mpd_substance (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    code        TEXT UNIQUE NOT NULL,
    source_id   BIGINT REFERENCES mpd_source(id) ON DELETE RESTRICT,
    name_inn    TEXT,
    name_en     TEXT,
    name        TEXT,
    addiction_category_id BIGINT REFERENCES mpd_addiction_category(id) ON DELETE SET NULL,
    doping_category_id BIGINT REFERENCES mpd_doping_category(id) ON DELETE SET NULL,
    government_regulation_category_id BIGINT REFERENCES mpd_government_regulation_category(id) ON DELETE SET NULL
);

-- 19) mpd_substance_synonym
CREATE TABLE IF NOT EXISTS mpd_substance_synonym (
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen       DATE NOT NULL,
    missing_since    DATE,
    substance_id     BIGINT NOT NULL REFERENCES mpd_substance(id) ON DELETE CASCADE,
    sequence_number  INT,
    source_id        BIGINT NOT NULL REFERENCES mpd_source(id) ON DELETE RESTRICT,
    name             TEXT,
    CONSTRAINT uk_substance_sequence_source UNIQUE (substance_id, sequence_number, source_id)
);

-- 20) mpd_medicinal_product
CREATE TABLE IF NOT EXISTS mpd_medicinal_product (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen DATE NOT NULL,
    missing_since DATE,
    sukl_code TEXT NOT NULL UNIQUE,
    reporting_obligation BOOLEAN NOT NULL,
    name TEXT NOT NULL,
    strength TEXT,
    dosage_form_id BIGINT REFERENCES mpd_dosage_form(id),
    packaging TEXT,
    administration_route_id BIGINT REFERENCES mpd_administration_route(id),
    supplementary_information TEXT,
    package_type_id BIGINT REFERENCES mpd_package_type(id),
    marketing_authorization_holder_id BIGINT REFERENCES mpd_organisation(id),
    current_marketing_authorization_holder_id BIGINT REFERENCES mpd_organisation(id),
    registration_status_id BIGINT REFERENCES mpd_registration_status(id),
    registration_valid_to DATE,
    registration_unlimited BOOLEAN NOT NULL,
    market_supply_end_date DATE,
    indication_group_id BIGINT REFERENCES mpd_indication_group(id),
    atc_group_id BIGINT REFERENCES mpd_atc_group(id),
    registration_number TEXT,
    parallel_import_id TEXT,
    parallel_import_supplier_id BIGINT REFERENCES mpd_organisation(id),
    registration_process_id BIGINT REFERENCES mpd_registration_process(id),
    daily_dose_amount NUMERIC(6,3),
    daily_dose_unit_id BIGINT REFERENCES mpd_measurement_unit(id),
    daily_dose_packaging NUMERIC(11,4),
    who_source TEXT,
    substance_list TEXT,
    dispense_type_id BIGINT REFERENCES mpd_dispense_type(id),
    addiction_category_id BIGINT REFERENCES mpd_addiction_category(id),
    doping_category_id BIGINT REFERENCES mpd_doping_category(id),
    government_regulation_category_id BIGINT REFERENCES mpd_government_regulation_category(id),
    deliveries_flag BOOLEAN NOT NULL,
    ean TEXT,
    braille TEXT,
    expiry_period_duration TEXT,
    expiry_period_unit TEXT,
    registered_name TEXT,
    mrp_number TEXT,
    registration_legal_basis TEXT,
    safety_feature BOOLEAN NOT NULL,
    prescription_restriction BOOLEAN NOT NULL,
    medicinal_product_type TEXT
);

-- 21) mpd_registration_exception
CREATE TABLE IF NOT EXISTS mpd_registration_exception (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen DATE NOT NULL,
    missing_since DATE,
    medicinal_product_id BIGINT NOT NULL REFERENCES mpd_medicinal_product(id) ON DELETE CASCADE,
    valid_from      DATE NOT NULL,
    valid_to        DATE,
    allowed_package_count INTEGER,
    purpose TEXT,
    workplace TEXT,
    distributor TEXT,
    note TEXT,
    submitter TEXT,
    manufacturer TEXT,
    CONSTRAINT uk_med_product_first_seen UNIQUE (medicinal_product_id, valid_from)
);

-- 22) mpd_cancelled_registration
CREATE TABLE IF NOT EXISTS mpd_cancelled_registration (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_seen  DATE NOT NULL,
    missing_since DATE,
    name        TEXT,
    administration_route_id BIGINT REFERENCES mpd_administration_route(id),
    dosage_form_id  BIGINT REFERENCES mpd_dosage_form(id),
    strength        TEXT,
    registration_number TEXT,
    parallel_import_id TEXT,
    mrp_number      TEXT,
    registration_process_id BIGINT REFERENCES mpd_registration_process(id),
    registration_legal_basis TEXT,
    marketing_authorization_holder_id BIGINT REFERENCES mpd_organisation(id),
    registration_end_date DATE,
    registration_status_id BIGINT REFERENCES mpd_registration_status(id),
    CONSTRAINT uk_cancelled_registration_number UNIQUE (registration_number, parallel_import_id)
);

-- 23) mpd_medicinal_product_substance
CREATE TABLE mpd_medicinal_product_substance (
    id BIGSERIAL PRIMARY KEY,
    first_seen DATE NOT NULL,
    missing_since DATE,

    medicinal_product_id BIGINT NOT NULL REFERENCES mpd_medicinal_product(id),
    substance_id BIGINT NOT NULL REFERENCES mpd_substance(id),

    sequence_number INT,
    composition_flag_id BIGINT REFERENCES mpd_composition_flag(id),
    amount_from TEXT,
    amount_to TEXT,
    measurement_unit_id BIGINT REFERENCES mpd_measurement_unit(id),

    related_to_id BIGINT REFERENCES mpd_medicinal_product_substance(id),
    relation_type TEXT CHECK (relation_type IN ('OR', 'CORRESPONDING_TO', 'CORRESPONDED_BY')),

    CONSTRAINT uk_mpd_medicinal_product_substance UNIQUE (medicinal_product_id, substance_id)
);

