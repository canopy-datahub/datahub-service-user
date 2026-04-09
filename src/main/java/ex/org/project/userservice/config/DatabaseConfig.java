package ex.org.project.userservice.config;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
@Configuration @Profile("!local")
public class DatabaseConfig {

    @Autowired
    Environment env;

    private String dbUser, dbPassword, host, port, dbName, dbDriverClassName;

    @Autowired
    DatabaseConfig(Environment env) {
        this.dbUser = env.getProperty("dbuser");
        this.dbPassword = env.getProperty("password");
        this.host = env.getProperty("host");
        this.port = env.getProperty("port");
        this.dbName = env.getProperty("dbname");
        this.dbDriverClassName = env.getProperty("dbDriverClassName");
    }

    @Primary @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder
                .create().driverClassName(dbDriverClassName)
                .username(this.dbUser).password(this.dbPassword)
                .url("jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.dbName);
        return dataSourceBuilder.build();
    }
}
