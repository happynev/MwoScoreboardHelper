ALTER TABLE MATCH_DATA DROP COLUMN REWARD_CBILLS;
ALTER TABLE MATCH_DATA DROP COLUMN REWARD_XP;
DROP TABLE MAP_DATA;
ALTER TABLE MATCH_DATA ALTER COLUMN MATCHRESULT VARCHAR(100);
CREATE TABLE PERSONAL_MATCHDATA (
   MATCH_DATA_ID int  NOT NULL,
   PLAYER_DATA_ID int  NOT NULL,
   REWARD_CBILLS int  NOT NULL,
   REWARD_XP int  NOT NULL,
   STAT_SOLO int  NOT NULL,
   STAT_KMDD int  NOT NULL,
   STAT_COMP int  NOT NULL,
   CONSTRAINT PERSONAL_MATCHDATA_pk PRIMARY KEY (MATCH_DATA_ID,PLAYER_DATA_ID)
);
ALTER TABLE PERSONAL_MATCHDATA ADD CONSTRAINT PERSONAL_MATCHDATA_MATCH_DATA
    FOREIGN KEY (MATCH_DATA_ID)
    REFERENCES MATCH_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;
ALTER TABLE PERSONAL_MATCHDATA ADD CONSTRAINT PERSONAL_MATCHDATA_PLAYER_DATA
    FOREIGN KEY (PLAYER_DATA_ID)
    REFERENCES PLAYER_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;
ALTER TABLE MATCH_DATA ADD COLUMN    TEAMSCORE varchar(15)  NOT NULL DEFAULT '';
ALTER TABLE MATCH_DATA ADD COLUMN    ENEMYSCORE varchar(15)  NOT NULL DEFAULT '';
