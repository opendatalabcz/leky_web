ALTER TABLE mpd_record_temporary_absence
    ALTER COLUMN dataset_type TYPE TEXT;

ALTER TABLE mpd_record_temporary_absence
    ADD CONSTRAINT chk_dataset_type_valid CHECK (
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
    );
