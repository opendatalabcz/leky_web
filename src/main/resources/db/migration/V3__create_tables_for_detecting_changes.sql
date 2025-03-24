CREATE TABLE mpd_attribute_change (
    id BIGSERIAL PRIMARY KEY,
    dataset_type TEXT NOT NULL,
    record_id BIGINT NOT NULL,
    attribute TEXT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    seen_in_dataset_valid_from DATE NOT NULL,
    CONSTRAINT chk_dataset_type_valid CHECK (
        dataset_type IN (
            'MPD_ACTIVE_SUBSTANCE',
            'MPD_ADDICTION_CATEGORY',
            'MPD_ADMINISTRATION_ROUTE',
            'MPD_ATC_GROUP',
            'MPD_CANCELLED_REGISTRATION',
            'MPD_COMPOSITION_FLAG',
            'MPD_COUNTRY',
            'MPD_DISPENSE_TYPE',
            'MPD_DOPING_CATEGORY',
            'MPD_DOSAGE_FORM',
            'MPD_GOVERNMENT_REGULATION_CATEGORY',
            'MPD_INDICATION_GROUP',
            'MPD_MEASUREMENT_UNIT',
            'MPD_MEDICINAL_PRODUCT',
            'MPD_ORGANISATION',
            'MPD_PACKAGE_TYPE',
            'MPD_REGISTRATION_EXCEPTION',
            'MPD_REGISTRATION_PROCESS',
            'MPD_REGISTRATION_STATUS',
            'MPD_SOURCE',
            'MPD_SUBSTANCE',
            'MPD_SUBSTANCE_SYNONYM'
        )
    )
);

CREATE TABLE mpd_record_temporary_absence (
    id BIGSERIAL PRIMARY KEY,
    dataset_type TEXT NOT NULL,
    record_id BIGINT NOT NULL,
    missing_from DATE NOT NULL,
    missing_to DATE NOT NULL,
    CONSTRAINT chk_dataset_type_valid_absence CHECK (
        dataset_type IN (
            'MPD_ACTIVE_SUBSTANCE',
            'MPD_ADDICTION_CATEGORY',
            'MPD_ADMINISTRATION_ROUTE',
            'MPD_ATC_GROUP',
            'MPD_CANCELLED_REGISTRATION',
            'MPD_COMPOSITION_FLAG',
            'MPD_COUNTRY',
            'MPD_DISPENSE_TYPE',
            'MPD_DOPING_CATEGORY',
            'MPD_DOSAGE_FORM',
            'MPD_GOVERNMENT_REGULATION_CATEGORY',
            'MPD_INDICATION_GROUP',
            'MPD_MEASUREMENT_UNIT',
            'MPD_MEDICINAL_PRODUCT',
            'MPD_ORGANISATION',
            'MPD_PACKAGE_TYPE',
            'MPD_REGISTRATION_EXCEPTION',
            'MPD_REGISTRATION_PROCESS',
            'MPD_REGISTRATION_STATUS',
            'MPD_SOURCE',
            'MPD_SUBSTANCE',
            'MPD_SUBSTANCE_SYNONYM'
        )
    )
);
