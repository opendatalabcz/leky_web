ALTER TABLE processed_dataset DROP CONSTRAINT processed_dataset_dataset_type_check;

ALTER TABLE processed_dataset ADD CONSTRAINT processed_dataset_dataset_type_check
CHECK (dataset_type IN
    ('ERECEPT_PREDPIS', 'ERECEPT_VYDEJ',
     'DISTRIBUCE_REG', 'DISTRIBUCE_DIS',
     'DISTRIBUCE_DIS_ZAHRANICI', 'DISTRIBUCE_LEK', 'MPD')
);
