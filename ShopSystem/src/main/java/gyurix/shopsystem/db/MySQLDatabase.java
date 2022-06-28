package gyurix.shopsystem.db;

import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor
public class MySQLDatabase {
    public String database;
    public String table;
    private transient Connection con;
    private String host;
    private String password;
    private String username;

    public MySQLDatabase(String host, String database, String username, String password, String table) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        this.table = table;
        openConnection();
    }

    public MySQLDatabase(ConfigurationSection mysql) {
        this(mysql.getString("host"), mysql.getString("database"), mysql.getString("username"), mysql.getString("password"), mysql.getString("table"));
    }

    public void close() {
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean command(String cmd, Object... args) {
        try (PreparedStatement st = prepare(cmd, args)) {
            return st.execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public void query(String cmd, ResultHandler rh, Object... args) {
        try (PreparedStatement st = prepare(cmd, args)) {
            try (ResultSet rs = st.executeQuery()) {
                rh.handle(rs);
            }
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    public int update(String cmd) {
        try (PreparedStatement st = getConnection().prepareStatement(cmd)) {
            return st.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int update(String cmd, Object... args) {
        try (PreparedStatement st = prepare(cmd, args)) {
            return st.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @return - The Connection
     */
    private Connection getConnection() {
        try {
            int timeout = 10000;
            if (con == null || !con.isValid(timeout)) {
                openConnection();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Opens the MySQL connection
     */
    private void openConnection() {
        try {
            if (con != null)
                con.close();
            con = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true&useSSL=false", username, password);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement prepare(String cmd, Object... args) throws Throwable {
        PreparedStatement st = getConnection().prepareStatement(cmd);
        for (int i = 0; i < args.length; ++i)
            st.setObject(i + 1, args[i] instanceof Enum ? ((Enum) args[i]).name() :
                String.valueOf(args[i]));
        return st;
    }
}

