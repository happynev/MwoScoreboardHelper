package at.happynev.mwoscoreboardhelper.junit;

import at.happynev.mwoscoreboardhelper.DbHandler;
import at.happynev.mwoscoreboardhelper.Logger;
import at.happynev.mwoscoreboardhelper.MechRuntime;
import at.happynev.mwoscoreboardhelper.Utils;
import at.happynev.mwoscoreboardhelper.tracer.ValueHelpers;
import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by Nev on 13.02.2017.
 */
public class SomeTests {
    @BeforeClass
    public static void init() throws SQLException {
        Server dbserver = Server.createTcpServer("-tcpPort", "9124", "-tcpAllowOthers", "-baseDir", Utils.getHomeDir().toString()).start();
        Logger.log("db server: " + dbserver.getURL());
        DbHandler.getInstance();//pre-init
    }

    @Test
    public void testMechDetectionNormal() {
        Assert.assertEquals("WHM-6D", MechRuntime.findMatchingMech("WHM-6D"));
        Assert.assertEquals("WHM-6D", MechRuntime.findMatchingMech("WHM-8D"));
        Assert.assertEquals("WHM-6D", MechRuntime.findMatchingMech("WHM-GD"));
        Assert.assertEquals("KGC-000B", MechRuntime.findMatchingMech("KGC-OOOB"));
        Assert.assertEquals("UM-R60", MechRuntime.findMatchingMech("UM-RGU"));
        Assert.assertEquals("MDD-B", MechRuntime.findMatchingMech("MIJIJ-B"));
        //TEE-PRIME-->EXE-PRIME ???
    }

    @Test
    public void testMechDetectionSpecial() {
        Assert.assertEquals("CN9-A(NCIX)", MechRuntime.findMatchingMech("CN9-A(NCIX)"));
        Assert.assertEquals("CN9-A(NCIX)", MechRuntime.findMatchingMech("CN9-A(NClX)"));
        Assert.assertEquals("CN9-A(NCIX)", MechRuntime.findMatchingMech("CN9-WNClX)"));
        Assert.assertEquals("MAD-IIC", MechRuntime.findMatchingMech("MAD-IIC(S)"));
        Assert.assertEquals("MAD-IIC", MechRuntime.findMatchingMech("MAD-IICIS)"));
        Assert.assertEquals("MAD-3R", MechRuntime.findMatchingMech("MAD-3R5)"));
        Assert.assertEquals("BLR-1G", MechRuntime.findMatchingMech("BLR-1G(P)"));
    }

    @Test
    public void testMechDetectionLoyaltyKnown() {
        Assert.assertEquals("HBR-F(L)", MechRuntime.findMatchingMech("HBR-F(L)"));
        Assert.assertEquals("HBR-F(L)", MechRuntime.findMatchingMech("HBR-E(L)"));
        Assert.assertEquals("CTF-3L(L)", MechRuntime.findMatchingMech("CTF-3L(L)"));
    }

    @Test
    public void testMechDetectiionLoyaltyUnknown() {
        Assert.assertEquals("AS7-K", MechRuntime.findMatchingMech("AS7-K(L)"));
        Assert.assertEquals("KGC-000B", MechRuntime.findMatchingMech("KGC-000B(L)"));
        Assert.assertEquals("KGC-000B", MechRuntime.findMatchingMech("KGC-OOOB(L)"));
    }

    @Test
    public void testGameModeDetection() {
        Assert.assertEquals("DOMINATION", ValueHelpers.guessValue("D0M INATI0N", ValueHelpers.ValueList.GAMEMODE.getItems()));
        Assert.assertEquals("SKIRMISH", ValueHelpers.guessValue(" SKIRMISH", ValueHelpers.ValueList.GAMEMODE.getItems()));
    }
}
