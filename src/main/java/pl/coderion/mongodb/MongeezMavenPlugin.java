package pl.coderion.mongodb;

import com.mongodb.Mongo;
import com.mongodb.MongoURI;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mongeez.Mongeez;
import org.mongeez.MongoAuth;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Mojo(name = "update")
public class MongeezMavenPlugin extends AbstractMojo {

    @Parameter(defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "src/main/mongeez/mongeez.xml")
    private File changeLogFile;

    @Parameter(defaultValue = "")
    private String dbUri;

    @Parameter(defaultValue = "localhost")
    private String dbHostName;

    @Parameter(defaultValue = "test")
    private String dbName;

    @Parameter(defaultValue = "27017")
    private String dbPort;

    @Parameter(defaultValue = "false")
    private Boolean dbAuth;

    @Parameter
    private String username;

    @Parameter
    private String password;

    @Component(role = org.sonatype.plexus.components.sec.dispatcher.SecDispatcher.class, hint = "default")
    private SecDispatcher securityDispatcher;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            // use the property for parent projects (pom only) or for ignoring execution completely
            if (skip) {
                getLog().info("Skip Property found .. not executing.");
            }
            else {
                try {
                    setPassword(securityDispatcher.decrypt(getPassword()));
                } catch (final SecDispatcherException e) {
                    throw new MojoExecutionException(e.getMessage());
                }

                final Mongeez mongeez = new Mongeez();
                mongeez.setFile(new FileSystemResource(changeLogFile));
                if(null != getDbUri() && !getDbUri().isEmpty()) {
                	MongoURI mongoURI = new  MongoURI(getDbUri());
                    mongeez.setMongo(new Mongo(mongoURI));
                } else {
                    mongeez.setMongo(new Mongo(getDbHostName(), Integer.valueOf(getDbPort())));
                }
                mongeez.setDbName(getDbName());

                if (Boolean.TRUE.equals(getDbAuth())) {
                    mongeez.setAuth(new MongoAuth(getUsername(), getPassword(), getDbName()));
                }

                mongeez.process();
            }
        } catch (final Exception e) {
            getLog().error(String.format("An unknown error occured: %s", e.getMessage()));
            throw new RuntimeException();
        }
    }

    public String getDbUri() {
		return dbUri;
	}

	public void setDbUri(String dbUri) {
		this.dbUri = dbUri;
	}

	public String getDbHostName() {
        return dbHostName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getDbAuth() {
        return dbAuth;
    }

    public void setDbHostName(String dbHostName) {
        this.dbHostName = dbHostName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDbAuth(Boolean dbAuth) {
        this.dbAuth = dbAuth;
    }
}
