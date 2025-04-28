import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.Scanner;


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

            var nastavi = true;
            while (nastavi) {
                try {
                    ispisiIzbornik();
                    nastavi = izvrsiIzbor(izbor(), statement);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            System.out.println("Kraj");

        } catch (SQLException e) {
            System.err.println("Greška prilikom spajanja na bazu podataka");
            e.printStackTrace();
        }
    }

    private static boolean izvrsiIzbor(int n, Statement statement) throws SQLException, Exception{
        boolean nastavi = true;

        switch (n) {
            case 1 -> unosDrzaveIzbor(statement);
            case 2 -> izmjenaDrzaveIzbor(statement);
            case 3 -> brisanjeDrzaveIzbor(statement);
            case 4 -> ispisiDrzavePaginirano(statement);
            case 5 -> nastavi = false;
            default -> System.out.println("Nepostojeci izbor");
        }

        return nastavi;
    }


    private static void ispisiIzbornik() {
        System.out.println("1 za unos");
        System.out.println("2 za izmjenu");
        System.out.println("3 za brisanje");
        System.out.println("4 za prikaz drzava");
        System.out.println("5 za kraj");
    }

    private static int izbor() {
        Scanner scanner = new Scanner(System.in);
        var broj = scanner.nextInt();
        scanner.nextLine();
        return broj;
    }

    private static void izmjenaDrzave(Statement statement, int ID, String naziv) throws SQLException {
        statement.executeUpdate(
                String.format("update Drzava set Naziv='%s' where IDDrzava=%s",naziv, ID)
        );
    }

    private static void brisanjeDrzave(Statement statement, int ID) throws SQLException {
        statement.executeUpdate(
                String.format("DELETE FROM Drzava WHERE IDDrzava=%s", ID)
        );
    }

    private static void unosDrzave(Statement statement, String naziv) throws SQLException {
        statement.executeUpdate(
                String.format("insert into Drzava(Naziv) values('%s')", naziv)
        );
    }

    private static void unosDrzaveIzbor(Statement statement) throws SQLException{
        System.out.println("Unesite naziv drzave");
        Scanner scanner = new Scanner(System.in);
        var naziv = scanner.nextLine();
        unosDrzave(statement, naziv);
    }

    private static void brisanjeDrzaveIzbor(Statement statement) throws SQLException, Exception {
        System.out.println("Unesite ID drzave koju zelite izbrisati");
        Scanner scanner = new Scanner(System.in);
        var ID = scanner.nextInt();
        scanner.nextLine();
        if (ID < 11) throw new Exception("Drzavu s tim IDem nije moguce mijenjati");

        ResultSet resultSet = dohvatiDrzavu(statement, ID);
        resultSet.next();
        var prethodniNaziv = resultSet.getString("Naziv");
        System.out.printf("Odabrana drzava je: %s\n", prethodniNaziv);
        System.out.println("Jeste li sigurni da zelite obrisati drzavu D?");
        var line = scanner.nextLine();
        if (line.toLowerCase().equals("d")) {
            brisanjeDrzave(statement, ID);
            System.out.println("Uspjesno ste izmijenili naziv drzave");
        }
        System.out.println();
    }

    private static void izmjenaDrzaveIzbor(Statement statement) throws SQLException, Exception{
        System.out.println("Unesite ID drzave koju zelite izmijeniti");
        Scanner scanner = new Scanner(System.in);
        var ID = scanner.nextInt();
        scanner.nextLine();

        if (ID < 11) throw new Exception("Drzavu s tim IDem nije moguce mijenjati");

        ResultSet resultSet = dohvatiDrzavu(statement, ID);
        resultSet.next();
        var prethodniNaziv = resultSet.getString("Naziv");
        System.out.printf("Odabrana drzava je: %s\n", prethodniNaziv);

        System.out.println("Za odustati birajte N, za unos unesite novi naziv");
        var line = scanner.nextLine();
        if (line.toLowerCase().equals("n")) {
            return;
        }
        izmjenaDrzave(statement, ID, line);
        System.out.println("Uspjesno ste izmijenili drzavu");
        System.out.println();

    }

    private static void printDrzavaPageN(Statement statement, int page, int pageSize, int pagesCount) throws SQLException {
        ispisiDrzave(dohvatiSlijedecihNDrzava(pageSize, page*pageSize, statement));
        System.out.println("Page "+(page+1)+"/"+pagesCount);
    }

    private static void ispisiDrzavePaginirano(Statement statement) throws SQLException {
        var pageSize = 3;
        var brojDrzava = countDrzave(statement);
        System.out.println("Ukupno " + brojDrzava + " drzava");

        int page = 0;
        int pagesCount = (int)(Math.ceil(brojDrzava/(float)pageSize));
        printDrzavaPageN(statement, page, pageSize, pagesCount);
        while (page < pagesCount -1) {
            System.out.println("Slijedeca stranica D?, za skok na stranicu unesite broj");
            Scanner scanner = new Scanner(System.in);
            var line = scanner.nextLine();
            if (line.toLowerCase().equals("d")) {
                page++;
                printDrzavaPageN(statement, page, pageSize, pagesCount);
                continue;
            }
            int n = Integer.parseInt(line) -1;
            if (n < pagesCount) {
                page = n;
                printDrzavaPageN(statement, page, pageSize, pagesCount);
            }
        }
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

    private static ResultSet dohvatiDrzavu(Statement statement, int ID) throws SQLException{
        return statement.executeQuery(
                String.format("Select Naziv from Drzava WHERE IDDrzava=%s", ID)
        );
    }

    private static ResultSet dohvatiSlijedecihNDrzava(int n, int offset, Statement statement) throws SQLException {
        return  statement.executeQuery(String.format(
                "SELECT * FROM Drzava ORDER BY Naziv OFFSET %s ROWS FETCH NEXT %s ROWS ONLY", offset,n
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