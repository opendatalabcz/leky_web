-- mpd_substance_synonym
ALTER TABLE mpd_substance_synonym
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_substance_synonym
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_substance_synonym
    ALTER COLUMN sequence_number DROP NOT NULL,
ALTER COLUMN name DROP NOT NULL;
ALTER TABLE mpd_substance_synonym
    ADD CONSTRAINT uk_substance_sequence_source UNIQUE (substance_id, sequence_number, source_id);

-- mpd_registration_exception
ALTER TABLE mpd_registration_exception
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_registration_exception
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_registration_exception
    ALTER COLUMN allowed_package_count DROP NOT NULL,
ALTER COLUMN purpose DROP NOT NULL,
    ALTER COLUMN workplace DROP NOT NULL,
    ALTER COLUMN distributor DROP NOT NULL,
    ALTER COLUMN note DROP NOT NULL,
    ALTER COLUMN submitter DROP NOT NULL,
    ALTER COLUMN manufacturer DROP NOT NULL;
ALTER TABLE mpd_registration_exception
    ADD CONSTRAINT uk_med_product_first_seen UNIQUE (medicinal_product_id, first_seen);

-- mpd_cancelled_registration
ALTER TABLE mpd_registration_exception
    ADD COLUMN first_seen DATE NOT NULL;
ALTER TABLE mpd_registration_exception
    ADD COLUMN missing_since DATE;
ALTER TABLE mpd_cancelled_registration
    ALTER COLUMN name DROP NOT NULL,
ALTER COLUMN strength DROP NOT NULL,
    ALTER COLUMN parallel_import_id DROP NOT NULL,
    ALTER COLUMN mrp_number DROP NOT NULL,
    ALTER COLUMN registration_legal_basis DROP NOT NULL,
    ALTER COLUMN registration_end_date DROP NOT NULL;
ALTER TABLE mpd_cancelled_registration
    ADD CONSTRAINT uk_cancelled_registration_number UNIQUE (registration_number, parallel_import_id);

-- mpd_substance
ALTER TABLE mpd_substance
    RENAME COLUMN valid_from TO first_seen;
ALTER TABLE mpd_substance
    RENAME COLUMN valid_to TO missing_since;
ALTER TABLE mpd_substance
    ALTER COLUMN name_inn DROP NOT NULL,
ALTER COLUMN name_en DROP NOT NULL,
    ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN source_id DROP NOT NULL;