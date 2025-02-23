CREATE TABLE IF NOT EXISTS erecept_prescription (
    district_code VARCHAR(10) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    sukl_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT pk_erecept_prescription PRIMARY KEY (district_code, year, month, sukl_code)
);

CREATE TABLE IF NOT EXISTS erecept_dispense (
    district_code VARCHAR(10) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    sukl_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT pk_erecept_dispense PRIMARY KEY (district_code, year, month, sukl_code)
);
