ALTER TABLE mpd_medicinal_product
    RENAME COLUMN valid_from TO first_seen;

ALTER TABLE mpd_medicinal_product
    RENAME COLUMN valid_to TO missing_since;

ALTER TABLE mpd_dosage_form
    RENAME COLUMN valid_from TO first_seen;

ALTER TABLE mpd_dosage_form
    RENAME COLUMN valid_to TO missing_since;

ALTER TABLE mpd_organisation
    RENAME COLUMN valid_from TO first_seen;

ALTER TABLE mpd_organisation
    RENAME COLUMN valid_to TO missing_since;
