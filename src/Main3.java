import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main3 {
    public static  void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        DataSource dataSource = createDataSource();
        createDataSource();
        try (Connection connection = dataSource.getConnection()){
            // try catch block za izvrsavanje transakcije
            transakcija(connection);
            System.out.println("Kraj");
        } catch (SQLException e) {
            System.err.println("Greška prilikom spajanja na bazu");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void transakcija(Connection connection) throws SQLException{
        try (Statement statement = connection.createStatement();Statement statement2 = connection.createStatement();) {
            connection.setAutoCommit(false); // iskljucivanje automatskog komita transakcije
            statement.executeUpdate("INSERT INTO Drzava(Naziv) VALUES(Nigerija)");
            statement2.executeUpdate("UPDATE Drzava SET Naziv = 'Germany' WHERE IDDrzava =2");
            //komitanje transakcije
            connection.commit();
            System.out.println("Transakcija izvrsena");
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("Transakcija ponistena");
            System.err.println(e.getMessage());
        }

        izlistajDrzave(connection);
    }

    public static void izlistajDrzave(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        //Dohvaćanje svih država
        ResultSet rs = stmt.executeQuery("SELECT IDDrzava, Naziv FROM Drzava");
        while (rs.next()) {
            System.out.printf("%d %s\n", rs.getInt("IDDrzava"), rs.getString("Naziv"));
        }
        rs.close();
        stmt.close();

    }

    public static DataSource createDataSource() {
        SQLServerDataSource ds = new SQLServerDataSource();
        try (
                FileReader fileReader = new FileReader(".env");
                BufferedReader br = new BufferedReader(fileReader)
        ) {
            ds.setServerName(br.readLine());
            ds.setPortNumber(Integer.parseInt(br.readLine()));
            ds.setDatabaseName(br.readLine());//
            ds.setUser(br.readLine());
            ds.setPassword(br.readLine());
            ds.setEncrypt(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ds;
    }

}
