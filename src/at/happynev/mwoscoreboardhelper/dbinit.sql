-- Created by Vertabelo (http://vertabelo.com)
-- Last modification date: 2017-02-18 07:43:25.881

-- tables
-- Table: MATCH_DATA
CREATE TABLE MATCH_DATA (
    ID int  NOT NULL DEFAULT seq.nextval,
    MATCHTIME timestamp  NOT NULL,
    GAMEMODE varchar(50)  NULL,
    MAP varchar(50)  NULL,
    MATCHRESULT varchar(100)  NULL,
    MATCHNAME varchar(50)  NOT NULL,
    BATTLETIME varchar(5)  NULL,
    MAPTIMEOFDAY varchar(5)  NULL,
    TEAMSCORE varchar(15)  NOT NULL,
    ENEMYSCORE varchar(15)  NOT NULL,
    CONSTRAINT MATCH_DATA_pk PRIMARY KEY (ID)
);

-- Table: MECH_DATA
CREATE TABLE MECH_DATA (
    API_ID varchar(30)  NOT NULL,
    INTERNAL_NAME varchar(25)  NULL,
    NAME varchar(25)  NULL,
    SHORT_NAME varchar(25)  NULL,
    CHASSIS varchar(30)  NULL,
    TONS int  NULL,
    MAX_SPEED double  NULL,
    MAX_ARMOR int  NULL,
    FACTION varchar(30)  NULL,
    SPECIALTYPE varchar(30)  NULL,
    CONSTRAINT MECH_DATA_pk PRIMARY KEY (API_ID)
);

-- Table: PERSONAL_MATCHDATA
CREATE TABLE PERSONAL_MATCHDATA (
    MATCH_DATA_ID int  NOT NULL,
    PLAYER_DATA_ID int  NOT NULL,
    HAS_REWARDS boolean  NOT NULL,
    REWARD_CBILLS int  NOT NULL,
    REWARD_XP int  NOT NULL,
    STAT_SOLO int  NOT NULL,
    STAT_KMDD int  NOT NULL,
    STAT_COMP int  NOT NULL,
    RATING_TEAM int  NULL,
    RATING_ENEMY int  NULL,
    RATING_MATCH int  NULL,
    CONSTRAINT PERSONAL_MATCHDATA_pk PRIMARY KEY (MATCH_DATA_ID,PLAYER_DATA_ID)
);

-- Table: PLAYER_DATA
CREATE TABLE PLAYER_DATA (
    ID int  NOT NULL DEFAULT seq.nextval,
    UNIT varchar(6)  NULL,
    PILOTNAME varchar(32)  NOT NULL,
    GUICOLOR_BACK varchar(10)  NULL,
    GUICOLOR_FRONT varchar(10)  NULL,
    NOTES varchar(10000)  NULL,
    ICON varchar(50)  NULL,
    SHORTNOTE varchar(200)  NULL,
    CONSTRAINT PLAYER_DATA_NAME_UNIQUE UNIQUE (PILOTNAME),
    CONSTRAINT PLAYER_DATA_pk PRIMARY KEY (ID)
);

-- Table: PLAYER_MATCHDATA
CREATE TABLE PLAYER_MATCHDATA (
    PLAYER_DATA_id int  NOT NULL,
    MATCH_DATA_id int  NOT NULL,
    MECH varchar(30)  NULL,
    STATUS varchar(30)  NULL,
    SCORE int  NULL,
    KILLS int  NULL,
    ASSISTS int  NULL,
    DAMAGE int  NULL,
    PING int  NULL,
    ENEMY boolean  NOT NULL,
    CONSTRAINT PLAYER_MATCHDATA_pk PRIMARY KEY (PLAYER_DATA_id,MATCH_DATA_id)
);

-- Table: PROCESSED
CREATE TABLE PROCESSED (
    FILENAME varchar(255)  NOT NULL,
    PROCESSING_TIME timestamp  NOT NULL,
    CONSTRAINT PROCESSED_pk PRIMARY KEY (FILENAME)
);

-- Table: SETTINGS
CREATE TABLE SETTINGS (
    propKey varchar(100)  NOT NULL,
    propValue varchar(200)  NOT NULL,
    CONSTRAINT SETTINGS_pk PRIMARY KEY (propKey)
);

-- foreign keys
-- Reference: PERSONAL_MATCHDATA_MATCH_DATA (table: PERSONAL_MATCHDATA)
ALTER TABLE PERSONAL_MATCHDATA ADD CONSTRAINT PERSONAL_MATCHDATA_MATCH_DATA
    FOREIGN KEY (MATCH_DATA_ID)
    REFERENCES MATCH_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;

-- Reference: PERSONAL_MATCHDATA_PLAYER_DATA (table: PERSONAL_MATCHDATA)
ALTER TABLE PERSONAL_MATCHDATA ADD CONSTRAINT PERSONAL_MATCHDATA_PLAYER_DATA
    FOREIGN KEY (PLAYER_DATA_ID)
    REFERENCES PLAYER_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;

-- Reference: PLAYER_MATCHDATA_MATCH_DATA (table: PLAYER_MATCHDATA)
ALTER TABLE PLAYER_MATCHDATA ADD CONSTRAINT PLAYER_MATCHDATA_MATCH_DATA
    FOREIGN KEY (MATCH_DATA_id)
    REFERENCES MATCH_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;

-- Reference: PLAYER_MATCHDATA_PLAYER_DATA (table: PLAYER_MATCHDATA)
ALTER TABLE PLAYER_MATCHDATA ADD CONSTRAINT PLAYER_MATCHDATA_PLAYER_DATA
    FOREIGN KEY (PLAYER_DATA_id)
    REFERENCES PLAYER_DATA (ID)
    ON DELETE  CASCADE
    ON UPDATE  CASCADE;

-- sequences
-- Sequence: seq
CREATE SEQUENCE seq
      INCREMENT BY 1
      MINVALUE 1
      MAXVALUE 9999999999999
      NO CYCLE;

-- End of file.

