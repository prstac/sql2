import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;

public class DBConnection {
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
