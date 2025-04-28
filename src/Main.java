import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        DataSource dataSource = createDataSource();
        try (Connection connection = dataSource.getConnection()){
            System.out.println("Uspješno ste spojeni na bazu podataka");
            Statement statement = connection.createStatement();
            /*
            int rowAffected =  statement.executeUpdate(
                    "update Drzava set Naziv='Hrvatska' where IdDrzava=1"
            );
            System.out.println(rowAffected);

*/
            statement.executeUpdate(
                    "insert into Drzava(naziv) values('Nikaragva')"
            );
            ResultSet resultSet = statement.executeQuery(
                    "Select IDDrzava,Naziv from Drzava"
            );
            ispisiDrzave(dohvatiDrzave(statement));

            statement.executeUpdate(
                    "delete from Drzava where Naziv='Nikaragva'"
            );
            ispisiDrzave(dohvatiDrzave(statement));

            var pageSize = 3;

            var brojDrzava = countDrzave(statement);
            System.out.println("Ukupno " + brojDrzava + " drzava");

            for (int page = 0; page < brojDrzava/pageSize; page++) {
                printDrzavaPageN(statement, page, pageSize, brojDrzava);
            }

        } catch (SQLException e) {
            System.err.println("Greška prilikom spajanja na bazu podataka");
            e.printStackTrace();
        }
    }

    private static void printDrzavaPageN(Statement statement, int page, int pageSize, int count) throws SQLException {
        System.out.println("Page "+(page+1)+"/"+count/pageSize);
        ispisiDrzave(dohvatiSlijedecihNDrzava(pageSize, page*pageSize, statement));
    }

    private static int countDrzave(Statement statement) throws SQLException {
        ResultSet resultSet  = statement.executeQuery(
                "Select Count(*) as broj from Drzava"
        );
        resultSet.next();
        return resultSet.getInt("broj");
    }


    private static ResultSet dohvatiDrzave(Statement statement) throws SQLException {
        return statement.executeQuery(
                "Select IDDrzava,Naziv from Drzava"
        );
    }

    private static ResultSet dohvatiSlijedecihNDrzava(int n, int offset, Statement statement) throws SQLException {
        return  statement.executeQuery(String.format(
                "SELECT * FROM Drzava ORDER BY IDDrzava OFFSET %s ROWS FETCH NEXT %s ROWS ONLY", offset,n
                )
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