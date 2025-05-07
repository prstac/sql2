import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Zadatak3 {
    public static void main(String[] args) {
        DataSource dataSource = DBConnection.createDataSource();
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

    public static void addToStavkaCijena(Connection connection, int value, int id) throws SQLException{
        PreparedStatement stmnt = connection.prepareStatement("UPDATE Stavka SET CijenaPoKomadu = CijenaPoKomadu + (?) WHERE IDStavka=(?)" );
        stmnt.setInt(1, value);
        stmnt.setInt(2, id);
        stmnt.executeUpdate();
    }

    public static void transakcija(Connection connection) throws SQLException{
        try {
            connection.setAutoCommit(false); // iskljucivanje automatskog komita transakcije

            addToStavkaCijena(connection,10, 8);
            addToStavkaCijena(connection, -10,9);

            //komitanje transakcije
            connection.commit();
            System.out.println("Transakcija izvrsena");
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("Transakcija ponistena");
            System.err.println(e.getMessage());
        }
    }
}
