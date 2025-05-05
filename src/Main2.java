import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main2 {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        List<String> drzave = new ArrayList<>();



        DataSource dataSource = createDataSource();
        try (Connection connection = dataSource.getConnection()){
            // insertSomePreparedStatement(connection);
            /*
            int id = 2;
            System.out.println("Ime kupca s id=" + id + " je: " +dohvatiKupca(connection, id));
            */

            Statement statement = connection.createStatement();

            System.out.println("Unesite Drzavu");
            int brojDrzavaZaUnos = 10;
            for (int i = 0; i < brojDrzavaZaUnos; i++) {
                drzave.add(scanner.nextLine());
            }
            unosDrzavaPrepredStatement(drzave, connection);
            ispisiDrzave(dohvatiDrzave(statement));

            //int idZadnjeDrzave = dohvatiZadnjaDrzavaID(connection);
            //int idZaPobrisati = idZadnjeDrzave - brojDrzavaZaUnos;

            pobrisiDrzaveOdIDa(connection, 12);

            ispisiDrzave(dohvatiDrzave(statement));

            System.out.println("Kraj");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


    }

    private static ResultSet dohvatiDrzave(Statement statement) throws SQLException {
        return statement.executeQuery(
                "Select IDDrzava,Naziv from Drzava"
        );
    }

    private static void ispisiDrzave(ResultSet resultSet) throws SQLException  {
        System.out.printf(" ID |  NAZIV\n");
        StringBuilder stringBuilder = new StringBuilder();
        var line = stringBuilder.repeat("-", 20);
        System.out.println(line);
        while (resultSet.next()) {
            System.out.printf("%3s | %s\n", String.format("%s", resultSet.getInt("IDDrzava")), resultSet.getString("Naziv"));
            System.out.println(line);
        }
    }

    private static void unosDrzavaPrepredStatement(List<String> drzave, Connection connection) throws SQLException {
        PreparedStatement stmnt = connection.prepareStatement("INSERT INTO Drzava (Naziv) VALUES (?)");
        for (String drzava : drzave) {
            stmnt.setString(1, drzava);
            stmnt.executeUpdate();
        }
    }

    private static void insertSomePreparedStatement(Connection connection) throws SQLException {
        ArrayList<Polaznik> polaznici = new ArrayList<>();
        polaznici.add(new Polaznik("Ana", "Ivić"));
        polaznici.add(new Polaznik("Marko", "Anić"));
        polaznici.add(new Polaznik("Ivica", "Doležal"));
        polaznici.add(new Polaznik("Ivana", "Benakić"));

        PreparedStatement stmnt = connection.prepareStatement("INSERT INTO Polaznik (Ime, Prezime) VALUES (?,?)");
        for (Polaznik polaznik : polaznici) {
            stmnt.setString(1, polaznik.getIme());
            stmnt.setString(2, polaznik.getPrezime());
            stmnt.executeUpdate();
        }
    }

    private static  String dohvatiKupca(Connection connection, int id) throws SQLException {
        CallableStatement callableStatement =  connection.prepareCall("{call DohvatiImeKupca(?,?)}");
        callableStatement.setInt(1, id);
        callableStatement.registerOutParameter(2, Types.NVARCHAR);
        callableStatement.executeUpdate();
        return callableStatement.getString(2);
    }

    /*
    private static int dohvatiZadnjaDrzavaID(Connection connection) throws SQLException {
        CallableStatement callableStatement =  connection.prepareCall("{call GetIDZadnjeDrzave(?)}");
        callableStatement.registerOutParameter(1, Types.INTEGER);
        callableStatement.executeUpdate();
        return callableStatement.getInt(1);
    }*/

    private static void pobrisiDrzaveOdIDa(Connection connection, int id) throws SQLException {
        CallableStatement callableStatement =  connection.prepareCall("{call ObrisiDrzave(?)}");
        callableStatement.setInt(1, id);
        callableStatement.executeUpdate();
    }

    private static DataSource createDataSource() {
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
