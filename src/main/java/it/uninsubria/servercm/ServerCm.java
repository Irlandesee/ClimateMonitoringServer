package it.uninsubria.servercm;

import java.net.Inet4Address;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerCm {

    private ServerSocket ss;
    private final int PORT = ServerInterface.PORT;
    private final String name = "ServerCm";
    private LinkedBlockingQueue<ServerSlave> slaves;
    protected static String dbUrl;

    private Properties props;
    private final Logger logger;
    protected static final String propertyDbDefaultUrl = "db.default_url";
    protected static final String propertyDbUrl = "db.url";
    protected static final String propertyDbUser = "db.username";
    protected static final String propertyDbPassword = "db.password";
    protected static final String propertyDefaultDbUser = "db.default_user";
    protected static final String propertyDefault_password = "db.default_password";
    protected static final String propertyDbDefaultName = "db.default_name";
    protected static final String propertyDbName = "db.name";

    private final ExecutorService clientHandler;
    private final ExecutorService connectionChecker;
    private final int MAX_NUMBER_OF_THREADS = 10;
    private final Scanner in;

    private final String separator;
    public ServerCm(){
        Path path = Paths.get("");
        FileSystem fs = path.getFileSystem();
        this.separator = fs.getSeparator();
        in = new Scanner(System.in);
        this.props = new Properties();
        initDb();
        try{
            ss = new ServerSocket(PORT);
            System.out.println("Server ip: " + Inet4Address.getLocalHost().getHostAddress());
            System.out.printf("%s lanciato sulla porta: %d\n", this.name, this.PORT);
            System.out.printf("%s dbUrl: %s\n", this.name, props.getProperty(propertyDbUrl));
        }catch(IOException ioe){
            System.err.println(ioe.getMessage());
        }

        clientHandler = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
        connectionChecker = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

        slaves = new LinkedBlockingQueue<ServerSlave>();
        this.logger = Logger.getLogger(this.name);
    }

    private void readDefaultProperties(){
        try(InputStream inputStream = ServerCm.class.getClassLoader().getResourceAsStream("db.properties")){
            if(inputStream == null){
                System.out.println("Il file db.properties non e' stato trovato, posizionarlo e rilanciare il programma...");
            }
            this.props.load(inputStream);
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }


    private void initDb(){
        System.out.println("Vuoi usare il file di credenziali user.config?");
        readDefaultProperties();

        if(readUserChoice().equals("y")){
            System.out.println("Leggo credenziali da user.properties");
            String currAbsPath = Paths.get("").toAbsolutePath() + this.separator + "config_files/user.properties";
            try(InputStreamReader inputStream = new InputStreamReader(new FileInputStream(currAbsPath))){
                this.props.load(inputStream);
            } catch (IOException e) {
                //System.out.println(e.getMessage());
                System.out.println("Il file user.config non e' stato trovato, uso il file di default db.properties...");
            }
        }else{
            System.out.println("Leggo credenziali da db.properties");
        }

        System.out.println(props);
        System.out.println("Cerco default db...");
        try(Connection masterConn = DriverManager.getConnection(
                props.getProperty(ServerCm.propertyDbDefaultUrl),
                props.getProperty(ServerCm.propertyDefaultDbUser),
                props.getProperty(ServerCm.propertyDefault_password))){
            System.out.println("Connessione con il db di default eseguita con successo");
            System.out.println("Cerco il db per ClimateMonitoring...");
            String dbName = props.getProperty(ServerCm.propertyDbName);
            String s = "select datname from pg_database where datname like '%s';".formatted(dbName);
            PreparedStatement checkDbQuery = masterConn.prepareStatement(s);
            ResultSet rSet = checkDbQuery.executeQuery();
            if(rSet.next()){
                String dbNameFound = rSet.getString("datname");
                if(dbNameFound.equals(dbName)){
                    System.out.println("Trovato database denominato: " + dbName);
                }
            }else{
                System.out.println("Nessun database denominato " + dbName + " trovato");
                System.out.println("Procedo con la creazione automatica?");
                String userChoice = readUserChoice();
                if(userChoice.equals("y")){
                    System.err.println("Creazione del database in corso...");
                    String createDbQuery = "create database %s".formatted(dbName);
                    PreparedStatement createDbStat = masterConn.prepareStatement(createDbQuery);
                    int result = createDbStat.executeUpdate();
                    try(Connection cmConn = DriverManager.getConnection(
                            props.getProperty(ServerCm.propertyDbUrl),
                            props.getProperty(ServerCm.propertyDefaultDbUser),
                            props.getProperty(ServerCm.propertyDefault_password)
                    )){
                        System.out.println(result+ ": Connessione con climate monitoring avvenuta con successo");
                        System.out.println("Creazione del db avvenuta...");
                        String dropOwnedOp = "do $$begin if exists (select from pg_roles where rolname = 'operatori') then execute 'drop owned by operatori'; end if; end$$";
                        String dropStat = "do $$begin if exists (select from pg_roles where rolname = 'server_slave') then execute 'drop owned by server_slave'; end if; end$$";
                        CallableStatement cStat = cmConn.prepareCall(dropStat);
                        cStat.executeUpdate();
                        CallableStatement opStat = cmConn.prepareCall(dropOwnedOp);
                        opStat.executeUpdate();
                        executeBatchSqlStatements(cmConn, "init.sql", 10);
                        System.out.println("Vuoi procedere con il popolamento demo?");
                        userChoice = readUserChoice();
                        if(userChoice.equals("y")){
                            executeBatchSqlStatements(cmConn, "cities.sql", 1000);
                            executeBatchSqlStatements(cmConn, "tables_demo.sql", 100);
                            System.out.println("Popolamento tabelle demo completato.");
                        }else{
                            System.out.println("Popolamento della tabella city in corso...");
                            executeBatchSqlStatements(cmConn, "cities.sql", 1000);
                            System.out.println("Popolamento della tabella city completato.");
                        }
                    }catch(SQLException sqle2){
                        System.err.println(sqle2.getMessage());
                        System.out.println("Tentativo di connessione con il db" + dbName + " fallito, verificarne la presenza con psql");
                        System.exit(1);
                    }

                }else{
                    System.out.println("Procedere manualmente con la creazione del database e poi riavviare il programma");
                    System.exit(0);
                }
            }
        }catch(SQLException sqle){
            System.err.println(sqle.getMessage());
            String postgresDownload = "https://www.postgresql.org/download/";
            System.out.println("In base al messaggio di errore, le credenziali potrebbero essere errate nel file di configurazione\n"
                    +"in tal caso, aprire il file di configurazione e settare le credenziali in modo corretto\n"
                    +"* postgres potrebbe non essere stato avviato/installato\n"
                    +"postgres download: "+postgresDownload+"\n");
            System.exit(1);
        }
    }

    private boolean isStringValid(String s){
        if(s.isEmpty()) return false;
        String regex = "^[a-zA-Z0-9_-]+$";
        Pattern VALID_STRING_PATTERN = Pattern.compile(regex);
        Matcher validStringMatcher = VALID_STRING_PATTERN.matcher(s);
        return !validStringMatcher.matches();
    }

    private String readUserChoice(){
        System.out.println("y/n?");
        String choice = in.next();
        if(choice.equals("y") || choice.equals("n")) return choice;
        return readUserChoice();
    }

    private List<String> parseSqlScriptFile(String fileName){
        List<String> sqlStatements = new LinkedList<String>();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(ServerCm.class.getClassLoader().getResourceAsStream(fileName))));
            StringBuilder currStat = new StringBuilder();
            String line;
            while((line = in.readLine()) != null){
                String regex = "--.*|/\\*(.|[\\r\\n])*?\\*/";
                Pattern COMMENT_PATTERN = Pattern.compile(regex);
                Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
                line = commentMatcher.replaceAll("").trim();
                if(!line.isEmpty()){
                    currStat.append(line).append(" ");
                    if(line.endsWith(";")){
                        sqlStatements.add(currStat.toString());
                        currStat.setLength(0);
                    }
                }
            }
            in.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return sqlStatements;
    }

    private String getMatch(String s, String regex){
        Pattern patter = Pattern.compile(regex);
        Matcher matcher = patter.matcher(s);
        return matcher.find() ? matcher.group(1) : "";
    }

    private void executeBatchSqlStatements(Connection conn, String fileName, int batchSize){
        List<String> sqlStatements = parseSqlScriptFile(fileName);
        int count = 0;
        try{
            Statement stat = conn.createStatement();
            for(String s : sqlStatements){
                if(s.contains("create table")){
                    String regex = "([^\\s]+)\\(";
                    System.err.println("Creando tabella: "+ getMatch(s, regex));
                }else if(s.contains("create role")){
                    String regex = "create role\\s+(\\S+)\\s+\\with";
                    System.err.println("Creando ruolo: "+getMatch(s, regex));
                }

                stat.addBatch(s);
                count++;
                if(count % batchSize == 0){
                    System.out.println("Executing batch");
                    stat.executeBatch();
                    stat.clearBatch();
                }
            }
            if(count % batchSize != 0) stat.executeBatch();
        }catch(SQLException sqle){
            System.err.println(sqle.getMessage());
        }
    }

    public static void main(String[] args){
        int i = 0;
        ServerCm serv = new ServerCm();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Master server shutting down...");
            serv.clientHandler.shutdown(); //close executor service
            serv.in.close(); //closes scanner
            try{
                serv.ss.close();
            } catch(IOException ioe){
                System.err.println(ioe.getMessage());
                System.err.println(Arrays.toString(ioe.getStackTrace()));
            }
        }));

        try{
            while(true){
                Socket sock = serv.ss.accept();
                ServerSlave serverSlave = new ServerSlave(sock, i, serv.props);
                serv.slaves.add(serverSlave);
                Future<?> future = serv.clientHandler.submit(serverSlave);

                serv.connectionChecker.execute(() -> {
                    try{
                        future.get();
                    }catch(InterruptedException | ExecutionException exception){System.err.println(exception.getMessage());}
                });
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }

    }


}
