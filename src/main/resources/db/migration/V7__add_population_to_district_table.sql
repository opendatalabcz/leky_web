ALTER TABLE district ADD COLUMN population INTEGER;

UPDATE district SET population = 0 WHERE code = '0000'; -- Unknown
UPDATE district SET population = 1357326 WHERE code = '3100'; -- Praha
UPDATE district SET population = 102228 WHERE code = '3201'; -- Benešov
UPDATE district SET population = 100517 WHERE code = '3202'; -- Beroun
UPDATE district SET population = 168708 WHERE code = '3203'; -- Kladno
UPDATE district SET population = 107268 WHERE code = '3204'; -- Kolín
UPDATE district SET population = 78072 WHERE code = '3205'; -- Kutná Hora
UPDATE district SET population = 113100 WHERE code = '3206'; -- Mělník
UPDATE district SET population = 133876 WHERE code = '3207'; -- Mladá Boleslav
UPDATE district SET population = 105463 WHERE code = '3208'; -- Nymburk
UPDATE district SET population = 198312 WHERE code = '3209'; -- Praha-východ
UPDATE district SET population = 158235 WHERE code = '3210'; -- Praha-západ
UPDATE district SET population = 117554 WHERE code = '3211'; -- Příbram
UPDATE district SET population = 56058 WHERE code = '3212'; -- Rakovník
UPDATE district SET population = 200426 WHERE code = '3301'; -- České Budějovice
UPDATE district SET population = 59756 WHERE code = '3302'; -- Český Krumlov
UPDATE district SET population = 89957 WHERE code = '3303'; -- Jindřichův Hradec
UPDATE district SET population = 73835 WHERE code = '3304'; -- Pelhřimov
UPDATE district SET population = 69888 WHERE code = '3305'; -- Písek
UPDATE district SET population = 49750 WHERE code = '3306'; -- Prachatice
UPDATE district SET population = 68262 WHERE code = '3307'; -- Strakonice
UPDATE district SET population = 89729 WHERE code = '3308'; -- Tábor
UPDATE district SET population = 55564 WHERE code = '3401'; -- Domažlice
UPDATE district SET population = 92829 WHERE code = '3402'; -- Cheb
UPDATE district SET population = 117939 WHERE code = '3403'; -- Karlovy Vary
UPDATE district SET population = 84685 WHERE code = '3404'; -- Klatovy
UPDATE district SET population = 185241 WHERE code = '3405'; -- Plzeň-město
UPDATE district SET population = 63231 WHERE code = '3406'; -- Plzeň-jih
UPDATE district SET population = 80410 WHERE code = '3407'; -- Plzeň-sever
UPDATE district SET population = 50721 WHERE code = '3408'; -- Rokycany
UPDATE district SET population = 85800 WHERE code = '3409'; -- Sokolov
UPDATE district SET population = 55126 WHERE code = '3410'; -- Tachov
UPDATE district SET population = 103483 WHERE code = '3501'; -- Česká Lípa
UPDATE district SET population = 127742 WHERE code = '3502'; -- Děčín
UPDATE district SET population = 118173 WHERE code = '3503'; -- Chomutov
UPDATE district SET population = 89647 WHERE code = '3504'; -- Jablonec nad Nisou
UPDATE district SET population = 179921 WHERE code = '3505'; -- Liberec
UPDATE district SET population = 114394 WHERE code = '3506'; -- Litoměřice
UPDATE district SET population = 85576 WHERE code = '3507'; -- Louny
UPDATE district SET population = 108420 WHERE code = '3508'; -- Most
UPDATE district SET population = 124374 WHERE code = '3509'; -- Teplice
UPDATE district SET population = 117984 WHERE code = '3510'; -- Ústí nad Labem
UPDATE district SET population = 93996 WHERE code = '3601'; -- Havlíčkův Brod
UPDATE district SET population = 164150 WHERE code = '3602'; -- Hradec Králové
UPDATE district SET population = 104736 WHERE code = '3603'; -- Chrudim
UPDATE district SET population = 80286 WHERE code = '3604'; -- Jičín
UPDATE district SET population = 108327 WHERE code = '3605'; -- Náchod
UPDATE district SET population = 179575 WHERE code = '3606'; -- Pardubice
UPDATE district SET population = 80196 WHERE code = '3607'; -- Rychnov nad Kněžnou
UPDATE district SET population = 74496 WHERE code = '3608'; -- Semily
UPDATE district SET population = 99280 WHERE code = '3609'; -- Svitavy
UPDATE district SET population = 114098 WHERE code = '3610'; -- Trutnov
UPDATE district SET population = 134331 WHERE code = '3611'; -- Ústí nad Orlicí
UPDATE district SET population = 108625 WHERE code = '3701'; -- Blansko
UPDATE district SET population = 385600 WHERE code = '3702'; -- Brno-město
UPDATE district SET population = 247599 WHERE code = '3703'; -- Brno-venkov
UPDATE district SET population = 114016 WHERE code = '3704'; -- Břeclav
UPDATE district SET population = 191377 WHERE code = '3705'; -- Zlín
UPDATE district SET population = 153191 WHERE code = '3706'; -- Hodonín
UPDATE district SET population = 116042 WHERE code = '3707'; -- Jihlava
UPDATE district SET population = 104398 WHERE code = '3708'; -- Kroměříž
UPDATE district SET population = 108633 WHERE code = '3709'; -- Prostějov
UPDATE district SET population = 110275 WHERE code = '3710'; -- Třebíč
UPDATE district SET population = 140811 WHERE code = '3711'; -- Uherské Hradiště
UPDATE district SET population = 97162 WHERE code = '3712'; -- Vyškov
UPDATE district SET population = 114928 WHERE code = '3713'; -- Znojmo
UPDATE district SET population = 117496 WHERE code = '3714'; -- Žďár nad Sázavou
UPDATE district SET population = 87867 WHERE code = '3801'; -- Bruntál
UPDATE district SET population = 209629 WHERE code = '3802'; -- Frýdek-Místek
UPDATE district SET population = 233674 WHERE code = '3803'; -- Karviná
UPDATE district SET population = 150498 WHERE code = '3804'; -- Nový Jičín
UPDATE district SET population = 232069 WHERE code = '3805'; -- Olomouc
UPDATE district SET population = 168125 WHERE code = '3806'; -- Opava
UPDATE district SET population = 279835 WHERE code = '3807'; -- Ostrava-město
UPDATE district SET population = 117777 WHERE code = '3808'; -- Přerov
UPDATE district SET population = 118323 WHERE code = '3809'; -- Šumperk
UPDATE district SET population = 139004 WHERE code = '3810'; -- Vsetín
UPDATE district SET population = 36192 WHERE code = '3811'; -- Jeseník

ALTER TABLE district
    ALTER COLUMN population SET NOT NULL;
