package org.manager.DBManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class RepositoryHelper {
    private static final Logger log = LoggerFactory.getLogger(RepositoryHelper.class);

    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    public static <T> List<T> queryList(
            Connection conn,
            String sql,
            SQLConsumer<PreparedStatement> params,
            SQLFunction<ResultSet, T> mapper
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null)
                params.accept(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) result.add(mapper.apply(rs));
                return result;
            }

        } catch (SQLException e) {
            log.error("Something went wrong with fetching the data: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T queryItem(
            Connection conn,
            String sql,
            SQLConsumer<PreparedStatement> params,
            SQLFunction<ResultSet, T> mapper
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null)
                params.accept(ps);

            try (ResultSet rs = ps.executeQuery()) {
                T result = null;
                while (rs.next()) result = mapper.apply(rs);
                return result;
            }

        } catch (SQLException e) {
            log.error("Something went wrong with fetching the data: {}", e.getMessage());
            return null;
        }
    }

    public static void update(
            Connection conn,
            String sql,
            SQLConsumer<PreparedStatement> params
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null)
                params.accept(ps);

            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Something went wrong with updating the data: {}", e.getMessage());
        }
    }

    public static void create(
            Connection conn,
            String sql
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            log.error("Something went wrong with creating the table: {}", e.getMessage());
        }
    }

    public static boolean checkForeignKey(
            Connection conn,
            String sql
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            log.error("Error checking foreign_keys existence: {}", e.getMessage());
            return false;
        }
    }

    public static boolean check(
            Connection conn,
            String sql
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            log.error("Error checking existence: {}", e.getMessage());
            return false;
        }
    }
}