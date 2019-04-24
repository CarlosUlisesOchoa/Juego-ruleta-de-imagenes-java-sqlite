/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import shane.Dialogs;

/**
 *
 * @author Uli Gibson
 */
public class DB {

    private String nombre;
    private Connection c = null;
    private Statement stmt = null;
    ResultSet rs = null;

    private File DB_file;

    public DB(String nombre_db) {
        this.nombre = nombre_db;
        DB_file = new File(nombre + ".db");
    }

    private void create() {
        if(DB_file.exists()) {
            this.connect();
            return;
        }
        
        try {
            Class.forName("org.sqlite.JDBC");

            c = DriverManager.getConnection("jdbc:sqlite:" + nombre+".db");

            stmt = c.createStatement();
            String sql = "CREATE TABLE DATA "
                    + "(NAME	TEXT PRIMARY KEY,"
                    + "SCORE	INTEGER NOT NULL)";

            stmt.executeUpdate(sql);
            sql =   "INSERT INTO DATA (NAME, SCORE) "
                    +"VALUES ('maquina', 0);";
            stmt.executeUpdate(sql);
            
            sql =   "INSERT INTO DATA (NAME, SCORE) "
                    +"VALUES ('usuario', 0);";
            stmt.executeUpdate(sql);
            

        } catch (ClassNotFoundException | SQLException e) {
            Dialogs.ErrorMsg("No se pudo crear la tabla.\n\n" + e.toString());
        }
    }

    public boolean connect() {
        if (!DB_file.exists()) {
            create();
        }
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + nombre+".db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            Dialogs.ErrorMsg("No se pudo conectar a la base de datos\n\n" + e.toString());
            return false;
        }
        return true;
    }
    
    public void close_conection()
    {
        try {
            stmt.close();
            c.commit();
            c.close();
        } catch (SQLException e) {
            Dialogs.ExceptionMsg(e);
        }
        
    }
    
    public int getScore(String target)
    {
        int score = -1;
        try {
            
            String sql = String.format(
                    "SELECT SCORE FROM DATA WHERE NAME = '%s'; ", target
            );
            rs = stmt.executeQuery(sql);

            rs.next();
            
            score = rs.getInt("SCORE");
            
            
        } catch (Exception e) {
            Dialogs.ExceptionMsg(e);
        }
        return score;
    }

    public boolean setScore(String target, int score) {
        try {
            String sql = String.format
                (
                    "UPDATE DATA SET SCORE = %d WHERE NAME = '%s'; ", score, target
                );
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            Dialogs.ExceptionMsg(e);
            return false;
        }
        return true;
    }



}
