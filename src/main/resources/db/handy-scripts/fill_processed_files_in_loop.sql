DO $$
DECLARE
    start_year INT := 2018;
    end_year INT := 2023;
    dataset_type TEXT;
    year INT;
    month INT;
BEGIN
    FOR dataset_type IN SELECT unnest(ARRAY['ERECEPT_PRESCRIPTION', 'ERECEPT_DISPENSE'])
    LOOP
        FOR year IN start_year..end_year LOOP
            FOR month IN 1..12 LOOP
                INSERT INTO processed_dataset (created_at, dataset_type, year, month)
                VALUES (CURRENT_TIMESTAMP, dataset_type, year, month);
            END LOOP;
        END LOOP;
    END LOOP;
END $$;
