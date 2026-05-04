package edu.unisabana.tyvs.registry.infrastructure.persistence;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class RegistryRepository implements RegistryRepositoryPort {
    private static final String TABLE_NAME = "registry";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            " id INT PRIMARY KEY," +
            " name VARCHAR(100) NOT NULL," +
            " age INT NOT NULL," +
            " is_alive BOOLEAN NOT NULL" +
            ");";
    private static final String EXISTS_BY_ID_SQL = "SELECT 1 FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + "(id, name, age, is_alive) VALUES(?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT id, name, age, is_alive FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public RegistryRepository(String jdbcUrl) {
        this(jdbcUrl, "", "");
    }

    public RegistryRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void initSchema() throws Exception {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible inicializar el esquema de persistencia", e);
        }
    }

    @Override
    public boolean existsById(int id) throws Exception {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(EXISTS_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible consultar existencia por id: " + id, e);
        }
    }

    @Override
    public void save(int id, String name, int age, boolean isAlive) throws Exception {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            boolean prev = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setInt(3, age);
                ps.setBoolean(4, isAlive);
                ps.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new IllegalStateException("No fue posible guardar el registro con id: " + id, e);
            } finally {
                con.setAutoCommit(prev);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Fallo de conexion al guardar registro", e);
        }
    }

    @Override
    public Optional<RegistryRecord> findById(int id) throws Exception {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new RegistryRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getBoolean("is_alive")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible buscar registro con id: " + id, e);
        }
    }

    @Override
    public void deleteAll() throws Exception {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate(DELETE_ALL_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible limpiar la tabla de registros", e);
        }
    }
}
