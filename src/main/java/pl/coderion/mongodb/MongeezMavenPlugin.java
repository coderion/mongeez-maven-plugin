package pl.coderion.mongodb;

import com.mongodb.Mongo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mongeez.Mongeez;
import org.mongeez.MongoAuth;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Mojo(name = "update")
public class MongeezMavenPlugin extends AbstractMojo {

    private static final String MONGODB_HOST_PROPERTY = "mongodb.host";
    private static final String MONGODB_PORT_PROPERTY = "mongodb.port";
    private static final String MONGODB_DATABASE_NAME_PROPERTY = "mongodb.database.name";
    private static final String MONGODB_USER_NAME = "mongodb.user.name";
    private static final String MONGODB_USER_PASSWD = "mongodb.user.password";
    private static final String MONGODB_EXEC_IGNORE = "mongodb.exec.ignore";

    @Parameter(property = "update.changeLogFile", defaultValue = "src/main/mongeez/mongeez.xml")
    private File changeLogFile;

    @Parameter(property = "update.propertyFile", defaultValue = "src/main/mongeez/config.properties")
    private File propertyFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            FileInputStream propertyFileInputStream = new FileInputStream(propertyFile);
            Properties properties = new Properties();
            properties.load(propertyFileInputStream);
            
            // use the property for parent projects (pom only) or for ignoring execution completely
            if (!StringUtils.isBlank(properties.getProperty(MongeezMavenPlugin.MONGODB_EXEC_IGNORE))) {
                getLog().info("Ignore Property found .. not executing.");
            } else {
                Mongeez mongeez = new Mongeez();
                mongeez.setFile(new FileSystemResource(changeLogFile));
                mongeez.setMongo(new Mongo(properties.getProperty(MONGODB_HOST_PROPERTY),
                        Integer.valueOf(properties.getProperty(MONGODB_PORT_PROPERTY))));

                if (!StringUtils.isBlank(properties.getProperty(MONGODB_USER_NAME)) &&
                        !StringUtils.isBlank(properties.getProperty(MONGODB_USER_PASSWD))) {
                    mongeez.setAuth(new MongoAuth(properties.getProperty(MONGODB_USER_NAME),
                            properties.getProperty(MONGODB_USER_PASSWD),
                            properties.getProperty(MONGODB_DATABASE_NAME_PROPERTY)));
                }

                mongeez.setDbName(properties.getProperty(MONGODB_DATABASE_NAME_PROPERTY));
                mongeez.process();
            }

        } catch (FileNotFoundException e) {
            getLog().error(String.format("Configuration file %s not found", propertyFile.getAbsolutePath()));
            throw new RuntimeException();

        } catch (IOException e) {
            getLog().error(String.format("An error occured during loading configuration file %s: %s",
                    propertyFile.getAbsolutePath(), e.getMessage()));
            throw new RuntimeException();

        } catch (Exception e) {
            getLog().error(String.format("An unknown error occured: %s", e.getMessage()));
            throw new RuntimeException();
        }
    }
}
