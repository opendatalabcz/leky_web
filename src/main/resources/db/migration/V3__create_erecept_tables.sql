CREATE TABLE IF NOT EXISTS district (
    code        VARCHAR(10) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    population  INTEGER      NOT NULL
);

INSERT INTO district (code, name, population) VALUES
    ('0000', 'Unknown',                0),

    ('3100', 'Praha',                  1357326),

    ('3201', 'Benešov',                102228),
    ('3202', 'Beroun',                 100517),
    ('3203', 'Kladno',                 168708),
    ('3204', 'Kolín',                  107268),
    ('3205', 'Kutná Hora',             78072),
    ('3206', 'Mělník',                 113100),
    ('3207', 'Mladá Boleslav',         133876),
    ('3208', 'Nymburk',                105463),
    ('3209', 'Praha-východ',           198312),
    ('3210', 'Praha-západ',            158235),
    ('3211', 'Příbram',                117554),
    ('3212', 'Rakovník',               56058),

    ('3301', 'České Budějovice',       200426),
    ('3302', 'Český Krumlov',          59756),
    ('3303', 'Jindřichův Hradec',      89957),
    ('3304', 'Pelhřimov',              73835),
    ('3305', 'Písek',                  69888),
    ('3306', 'Prachatice',             49750),
    ('3307', 'Strakonice',             68262),
    ('3308', 'Tábor',                  89729),

    ('3401', 'Domažlice',              55564),
    ('3402', 'Cheb',                   92829),
    ('3403', 'Karlovy Vary',           117939),
    ('3404', 'Klatovy',                84685),
    ('3405', 'Plzeň-město',            185241),
    ('3406', 'Plzeň-jih',              63231),
    ('3407', 'Plzeň-sever',            80410),
    ('3408', 'Rokycany',               50721),
    ('3409', 'Sokolov',                85800),
    ('3410', 'Tachov',                 55126),

    ('3501', 'Česká Lípa',             103483),
    ('3502', 'Děčín',                  127742),
    ('3503', 'Chomutov',               118173),
    ('3504', 'Jablonec nad Nisou',     89647),
    ('3505', 'Liberec',                179921),
    ('3506', 'Litoměřice',             114394),
    ('3507', 'Louny',                  85576),
    ('3508', 'Most',                   108420),
    ('3509', 'Teplice',                124374),
    ('3510', 'Ústí nad Labem',         117984),

    ('3601', 'Havlíčkův Brod',         93996),
    ('3602', 'Hradec Králové',         164150),
    ('3603', 'Chrudim',                104736),
    ('3604', 'Jičín',                  80286),
    ('3605', 'Náchod',                 108327),
    ('3606', 'Pardubice',              179575),
    ('3607', 'Rychnov nad Kněžnou',    80196),
    ('3608', 'Semily',                 74496),
    ('3609', 'Svitavy',                99280),
    ('3610', 'Trutnov',                114098),
    ('3611', 'Ústí nad Orlicí',        134331),

    ('3701', 'Blansko',                108625),
    ('3702', 'Brno-město',             385600),
    ('3703', 'Brno-venkov',            247599),
    ('3704', 'Břeclav',                114016),
    ('3705', 'Zlín',                   191377),
    ('3706', 'Hodonín',                153191),
    ('3707', 'Jihlava',                116042),
    ('3708', 'Kroměříž',               104398),
    ('3709', 'Prostějov',              108633),
    ('3710', 'Třebíč',                 110275),
    ('3711', 'Uherské Hradiště',       140811),
    ('3712', 'Vyškov',                 97162),
    ('3713', 'Znojmo',                 114928),
    ('3714', 'Žďár nad Sázavou',       117496),

    ('3801', 'Bruntál',                87867),
    ('3802', 'Frýdek-Místek',          209629),
    ('3803', 'Karviná',                233674),
    ('3804', 'Nový Jičín',             150498),
    ('3805', 'Olomouc',                232069),
    ('3806', 'Opava',                  168125),
    ('3807', 'Ostrava-město',          279835),
    ('3808', 'Přerov',                 117777),
    ('3809', 'Šumperk',                118323),
    ('3810', 'Vsetín',                 139004),
    ('3811', 'Jeseník',                36192);

CREATE TABLE IF NOT EXISTS erecept_prescription (
    id                   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    district_code        VARCHAR(10) NOT NULL REFERENCES district(code),
    year                 INT NOT NULL,
    month                INT NOT NULL,
    medicinal_product_id BIGINT NOT NULL REFERENCES mpd_medicinal_product(id),
    quantity             NUMERIC(10,3) NOT NULL
);

CREATE TABLE IF NOT EXISTS erecept_dispense (
    id                   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    district_code        VARCHAR(10) NOT NULL REFERENCES district(code),
    year                 INT NOT NULL,
    month                INT NOT NULL,
    medicinal_product_id BIGINT NOT NULL REFERENCES mpd_medicinal_product(id),
    quantity             NUMERIC(10,3) NOT NULL
);

