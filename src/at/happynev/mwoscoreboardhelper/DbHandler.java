package at.happynev.mwoscoreboardhelper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nev on 12.03.2016.
 */
public class DbHandler {
    public final static String EMPTY = "~empty~";
    private static DbHandler ourInstance;
    private final String dburl = "jdbc:h2:tcp://localhost:9124/mwoscoreboarddb";
    private Connection con = null;
    private Map<String, PreparedStatement> cachedStatements = new HashMap<>();
    private Map<PreparedStatement, Integer> statementUsage = new HashMap<>();
    private BooleanProperty writeEnabled = new SimpleBooleanProperty(true);

    private DbHandler() {
        writeEnabled.addListener((observable, oldValue, newValue) -> reopenConnection());
        reopenConnection();
    }

    public static DbHandler getInstance() {
        if (ourInstance == null) {
            ourInstance = new DbHandler();
        }
        return ourInstance;
    }

    public synchronized void dumpPreparedStatement(String sql) {
        if (cachedStatements.containsKey(sql)) {
            try {
                PreparedStatement prep = cachedStatements.get(sql);
                statementUsage.remove(prep);
                prep.close();
            } catch (SQLException e) {
                Logger.warning("failed to close prep stmt for " + sql);
            }
            cachedStatements.remove(sql);
        } else {
            Logger.warning("tried to dump non-existing sql:" + sql);
        }
    }

    private void saveSetting(String key, String value) {
        if (value == null || value.isEmpty()) {
            value = EMPTY;
        }
        try {
            PreparedStatement clean = prepareStatement("delete from SETTINGS where propKey=?");
            clean.setString(1, key);
            clean.executeUpdate();
            PreparedStatement insert = prepareStatement("insert into SETTINGS(propValue,propKey) values(?,?)");
            insert.setString(1, value);
            insert.setString(2, key);
            insert.executeUpdate();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    private String loadSetting(String key, String defaultValue) {
        String ret = defaultValue;
        try {
            PreparedStatement prep = prepareStatement("select propValue from SETTINGS where propKey=?");
            prep.setString(1, key);
            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                ret = rs.getString(1);
                if (ret.equals(EMPTY)) {
                    ret = "";
                }
            }
            rs.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
        return ret;
    }

    public boolean getWriteEnabled() {
        return writeEnabled.get();
    }

    public BooleanProperty writeEnabledProperty() {
        return writeEnabled;
    }

    private void reopenConnection() {
        try {
            if (con != null) {
                if (writeEnabled.getValue()) {
                    saveSetting("dbWriteEnabled", "true");
                    con.commit();
                } else {
                    con.rollback();
                    saveSetting("dbWriteEnabled", "false");
                    con.commit();
                }
            }
            Logger.log("reopen DB with writeEnabled= " + writeEnabled.getValue());
            if (con != null) con.close();
            cachedStatements.clear();
            con = DriverManager.getConnection(dburl);
            initializeTables();
            boolean autocommit = Boolean.parseBoolean(loadSetting("dbWriteEnabled", "true"));
            writeEnabled.set(autocommit);
            con.setAutoCommit(autocommit);
        } catch (Exception e) {
            Logger.dberror(e);
        }
    }

    private void initializeTables() throws SQLException {
        try {
            PreparedStatement prep = prepareStatement("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='PUBLIC' and TABLE_NAME='SETTINGS'");
            ResultSet rs = prep.executeQuery();
            if (!rs.next()) {
                RunScript.execute(con, new InputStreamReader(getClass().getResourceAsStream("dbinit.sql")));
            }
            rs.close();
            int version = Integer.parseInt(loadSetting("version", "0").replaceAll("\\..*", ""));
            if (version < Main.getDbVersion()) {
                RunScript.execute(con, new InputStreamReader(getClass().getResourceAsStream("dbUpgradeFromVersion" + version + ".sql")));
            }
            saveSetting("version", "" + Main.getDbVersion());
        } catch (SQLException e) {
            Logger.dberror(e);
        }
    }

    public synchronized String insertReturning(PreparedStatement stmt, String... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            String v = parameters[i];
            if ("".equals(v)) {
                //v = EMPTY;
            }
            stmt.setString(i + 1, v);
        }
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        String ret = rs.getString(1);
        rs.close();
        stmt.clearParameters();
        return ret;
    }

    public synchronized PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(sql, false);
    }

    public synchronized PreparedStatement prepareStatement(String sql, boolean returnKey) throws SQLException {
        PreparedStatement prep = cachedStatements.get(sql);
        if (prep == null || prep.isClosed()) {
            if (returnKey) {
                prep = con.prepareStatement(sql);
            } else {
                prep = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            cachedStatements.put(sql, prep);
            statementUsage.put(prep, 0);
        } else {
            prep.clearParameters();
        }
        statementUsage.put(prep, statementUsage.get(prep) + 1);
        if (statementUsage.get(prep) > 1000) {
            //safety precaution. sometimes prep statement gets closed inexplicably after lots of use
            dumpPreparedStatement(sql);
            prep = prepareStatement(sql, returnKey);
        }
        return prep;
    }

    public Connection getConnection() {
        return con;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
