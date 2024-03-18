package it.uninsubria.servercm;
import it.uninsubria.datamodel.areaInteresse.AreaInteresse;
import it.uninsubria.datamodel.centroMonitoraggio.CentroMonitoraggio;
import it.uninsubria.datamodel.city.City;
import it.uninsubria.factories.RequestFactory;
import it.uninsubria.datamodel.operatore.Operatore;
import it.uninsubria.datamodel.operatore.OperatoreAutorizzato;
import it.uninsubria.datamodel.parametroClimatico.NotaParametro;
import it.uninsubria.datamodel.parametroClimatico.ParametroClimatico;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.servercm.ServerInterface.Tables;
import it.uninsubria.servercm.ServerInterface.ResponseType;
import it.uninsubria.util.IDGenerator;
import javafx.util.Pair;


import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class Worker extends Thread{

    private static int workerCount = 0;
    private String clientId;
    private String requestId;
    private String responseId;
    private final String workerId;
    private final ServerCm server;
    private final String dbUrl;
    private final Properties props;
    private Connection conn;
    private Logger logger;

    public Worker(String workerId, String dbUrl, Properties props, ServerCm server){
        logger = Logger.getLogger(workerId);
        this.workerId = workerId;
        this.setName(workerId);
        this.server = server;
        this.dbUrl = dbUrl;
        this.props = props;
        try{
            this.conn = DriverManager.getConnection(dbUrl, props);
        }catch(SQLException sqle){sqle.printStackTrace();}
    }

    public void run(){
        System.out.printf("Worker %s started\n", workerId);
        System.out.printf("Worker %s getting request\n", workerId);
        //Request request = server.getRequest(this.workerId);
        Request request = null;
        this.clientId = request.getClientId();
        this.requestId = request.getRequestId();
        this.responseId = IDGenerator.generateID();
        System.out.printf("Worker %s serving client{%s} request: %s\n", workerId, clientId, requestId);
        //System.out.println("Request: " + request.toString());
        //Read the request
        Response res = null;
        switch(request.getRequestType()){
            //query the database
            case selectAll -> {
                res = selectAll(request);
                System.out.println("Response: " + res);
            }
            case selectAllWithCond -> {
                if(request.getParams().size() < ServerInterface.selectAllWithCondParamsLength){
                    res = new Response(clientId, requestId, responseId,  ResponseType.Error, request.getTable(), null);
                }else res = selectAllWithCond(request);

            }
            case selectObjWithCond -> {
                if(request.getParams().size() < ServerInterface.selectObjWithCondParamsLength){
                    res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                }
                else res = selectObjWithCond(request);

            }
            case selectObjJoinWithCond -> {
                if(request.getParams().size() < ServerInterface.selectObjJoinWithCondParamsLength){
                    res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                }else res = selectObjectJoinWithCond(request);
            }
            case executeLogin -> {
                if(request.getParams().size() < ServerInterface.executeLoginParamsLength){
                    res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                }else res = executeLogin(request);

            }
            case executeUpdateAi -> {
                if(request.getParams().size() < ServerInterface.executeUpdateParamsLength){
                    res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                }else res = executeUpdateAi(request);
            }
            case executeSignUp -> {
                if(request.getParams().size() < ServerInterface.executeSignUpParamsLength){
                    res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                }else res = insertOperatore(request);
            }
            case insert -> {
                switch(request.getTable()){
                    case AREA_INTERESSE -> {
                        if(request.getParams().size() < ServerInterface.insertAiParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertAreaInteresse(request);

                    }
                    case PARAM_CLIMATICO -> {
                        if(request.getParams().size() < ServerInterface.insertPcParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertParametroClimatico(request);

                    }
                    case OPERATORE -> {
                        if(request.getParams().size() < ServerInterface.insertOpParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertOperatore(request);
                    }
                    case OP_AUTORIZZATO -> {
                        if(request.getParams().size() < ServerInterface.insertAuthOpParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertOperatoreAutorizzato(request);
                    }
                    case NOTA_PARAM_CLIMATICO -> {
                        if(request.getParams().size() < ServerInterface.insertNpcParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertNotaParametroClimatico(request);
                    }
                    case CENTRO_MONITORAGGIO -> {
                        if(request.getParams().size() < ServerInterface.insertCmParamsLength){
                            res = new Response(clientId, requestId, responseId, ResponseType.Error, request.getTable(), null);
                        }else res = insertCentroMonitoraggio(request);
                    }
                }
            }
        }
        //save the result in the server's queue
        System.out.printf("Worker %s saving request %s in server's queue\n", this.workerId, request.getRequestId());
        //server.addResponse(res, this.workerId);
    }



    public ResultSet prepAndExecuteStatement(String query, String arg) throws SQLException{
        PreparedStatement stat = conn.prepareStatement(query);
        System.out.println(workerId + ":"+ stat);
        stat.setString(1, arg);
        return stat.executeQuery();
    }

    private String getQueryResult(String query, String oggetto){
        String result;
        System.out.println(workerId + ":" + query);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            if(rSet.next())
                return rSet.getString(oggetto);
        }catch(SQLException sqle){sqle.printStackTrace();}
        return null;
    }

    private ParametroClimatico extractParametroClimatico(ResultSet rSet) throws SQLException{
        ParametroClimatico pc = new ParametroClimatico(
                rSet.getString("parameterid"),
                rSet.getString("centroid"),
                rSet.getString("areaid"),
                rSet.getDate("pubdate").toLocalDate());
        pc.setVentoValue(rSet.getShort("valore_vento"));
        pc.setUmiditaValue(rSet.getShort("valore_umidita"));
        pc.setPressioneValue(rSet.getShort("valore_pressione"));
        pc.setTemperaturaValue(rSet.getShort("valore_temperatura"));
        pc.setPrecipitazioniValue(rSet.getShort("valore_precipitazioni"));
        pc.setAltitudineValue(rSet.getShort("valore_alt_ghiacciai"));
        pc.setMassaValue(rSet.getShort("valore_massa_ghiacciai"));
        return pc;
    }

    private AreaInteresse extractAreaInteresse(ResultSet rSet) throws SQLException{
        AreaInteresse ai = new AreaInteresse(
                rSet.getString("areaid"),
                rSet.getString("denominazione"),
                rSet.getString("stato"),
                rSet.getFloat("latitudine"),
                rSet.getFloat("longitudine"));
        return ai;
    }

    private NotaParametro extractNota(ResultSet rSet) throws SQLException{
        NotaParametro nota =
                new NotaParametro(
                        rSet.getString("notaid"),
                        rSet.getString("nota_vento"),
                        rSet.getString("nota_umidita"),
                        rSet.getString("nota_pressione"),
                        rSet.getString("nota_temperatura"),
                        rSet.getString("nota_precipitazioni"),
                        rSet.getString("nota_alt_ghiacciai"),
                        rSet.getString("nota_massa_ghiacciai"));
        return nota;
    }

    private CentroMonitoraggio extractCentroMonitoraggio(ResultSet rSet) throws SQLException{
        CentroMonitoraggio cm = new CentroMonitoraggio(
                rSet.getString("centroid"),
                rSet.getString("nomecentro"),
                rSet.getString("comune"),
                rSet.getString("country")
        );
        Array a = rSet.getArray("aree_interesse_ids");
        for(String s : (String[])a.getArray()){
            cm.putAreaId(s);
        }
        return cm;
    }

    private City extractCity(ResultSet rSet) throws SQLException{
        City c = new City(
                rSet.getString("geoname_id"),
                rSet.getString("ascii_name"),
                rSet.getString("country"),
                rSet.getString("country_code"),
                rSet.getFloat("latitude"),
                rSet.getFloat("longitude")
        );
        return c;
    }

    private Operatore extractOperatore(ResultSet rSet) throws SQLException{
        Operatore op = new Operatore(
                rSet.getString("nome"),
                rSet.getString("cognome"),
                rSet.getString("codice_fiscale"),
                rSet.getString("email"),
                rSet.getString("userid"),
                rSet.getString("password"),
                rSet.getString("centroid")
        );
        return op;
    }

    private OperatoreAutorizzato extractAuthOp(ResultSet rSet) throws SQLException{
        OperatoreAutorizzato authOp = new OperatoreAutorizzato(
                rSet.getString("codice_fiscale"),
                rSet.getString("email")
        );
        return authOp;
    }

    private Response selectAll(Request req){
        switch(req.getTable()){
            case CITY -> {
                List<City> res = new LinkedList<City>();
                String query = "select * from city";
                try(PreparedStatement stat = conn.prepareStatement(query)){
                    ResultSet rSet = stat.executeQuery();
                    while(rSet.next()){
                        City c = extractCity(rSet);
                        res.add(c);
                    }
                }catch(SQLException sqle){sqle.printStackTrace();}
                return new Response(clientId, requestId, responseId, ResponseType.List, req.getTable(), res);
            }
            case CENTRO_MONITORAGGIO -> {
                List<CentroMonitoraggio> res = new LinkedList<CentroMonitoraggio>();
                String query = "select * from centro_monitoraggio";
                try{
                    PreparedStatement stat = conn.prepareStatement(query);
                    ResultSet rSet = stat.executeQuery();
                    while(rSet.next()){
                        CentroMonitoraggio cm = extractCentroMonitoraggio(rSet);
                        res.add(cm);
                    }
                }catch(SQLException sqle){sqle.printStackTrace();}
                return new Response(clientId, requestId, responseId, ResponseType.List, Tables.CENTRO_MONITORAGGIO, res);
            }
            case AREA_INTERESSE -> {
                List<AreaInteresse> res = new LinkedList<AreaInteresse>();
                String query = "select * from area_interesse";
                try(PreparedStatement stat = conn.prepareStatement(query)){
                    ResultSet resultSet = stat.executeQuery();
                    while(resultSet.next()){
                        res.add(extractAreaInteresse(resultSet));
                    }
                }catch(SQLException sqle){sqle.printStackTrace();}
                return new Response(clientId, requestId, responseId, ResponseType.List, Tables.AREA_INTERESSE, res);
            }
            case OPERATORE -> {
                List<Operatore> res = new LinkedList<Operatore>();
                String query = "select * from operatore";
                try{
                    PreparedStatement stat = conn.prepareStatement(query);
                    ResultSet rSet = stat.executeQuery();
                    while(rSet.next()){
                        Operatore o = extractOperatore(rSet);
                        res.add(o);
                    }

                }catch(SQLException sqle){sqle.printStackTrace();}
                return new Response(clientId, requestId, responseId, ResponseType.List, Tables.OPERATORE, res);
            }
            case OP_AUTORIZZATO -> {
                List<OperatoreAutorizzato> res = new LinkedList<OperatoreAutorizzato>();
                String query = "select * from operatore_autorizzati";
                try{
                    PreparedStatement stat = conn.prepareStatement(query);
                    ResultSet rSet = stat.executeQuery();
                    while(rSet.next()){
                        OperatoreAutorizzato oAutorizzato = extractAuthOp(rSet);
                        res.add(oAutorizzato);
                    }

                }catch(SQLException sqle){sqle.printStackTrace();}
                return new Response(clientId, requestId, responseId, ResponseType.List, Tables.OP_AUTORIZZATO, res);
            }
            case PARAM_CLIMATICO -> {
                List<ParametroClimatico> res = new LinkedList<ParametroClimatico>();
                String query = "select * from parametro_climatico";
                try(PreparedStatement stat = conn.prepareStatement(query)){
                    ResultSet rSet = stat.executeQuery();
                    while(rSet.next()){
                        ParametroClimatico pc = extractParametroClimatico(rSet);
                        res.add(pc);
                    }
                }catch(SQLException sqle){sqle.printStackTrace(); }
                return new Response(clientId, requestId, responseId, ResponseType.List, Tables.PARAM_CLIMATICO, res);
            }
        }
        return new Response(clientId, requestId, responseId, ResponseType.Error, req.getTable(), null);
    }

    private Pair<ResponseType, List<City>> selectAllCityCond(String fieldCond, String cond){
        String query = "select * from city where " + fieldCond + " = ?";
        LinkedList<City> cities = new LinkedList<City>();
        try(ResultSet rSet = prepAndExecuteStatement(query, cond)){
            while(rSet.next()){
                City c = extractCity(rSet);
                cities.add(c);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<City>>(ResponseType.Error, cities);}
        if(cities.isEmpty()) return new Pair<ResponseType, List<City>>(ResponseType.NoSuchElement, cities);
        return new Pair<ResponseType, List<City>>(ResponseType.List, cities);
    }

     private Pair<ResponseType, List<CentroMonitoraggio>> selectAllCmCond(String fieldCond, String cond){
        String query = "select * from centro_monitoraggio where " + fieldCond + " = ?";
        List<CentroMonitoraggio> cms = new LinkedList<CentroMonitoraggio>();
        try(ResultSet rSet = prepAndExecuteStatement(query, cond)){
            while(rSet.next()){
                CentroMonitoraggio cm = extractCentroMonitoraggio(rSet);
                cms.add(cm);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<CentroMonitoraggio>>(ResponseType.Error, cms);}
        if(cms.isEmpty()) return new Pair<ResponseType, List<CentroMonitoraggio>>(ResponseType.NoSuchElement, cms);
        return new Pair<ResponseType, List<CentroMonitoraggio>>(ResponseType.List, cms);
    }

    private Pair<ResponseType, List<AreaInteresse>> selectAllAiCond(String fieldCond, String cond){
        String query = "select * from area_interesse where %s = '%s'".formatted(fieldCond, cond);
        System.out.println(query);
        List<AreaInteresse> areeInteresse = new LinkedList<AreaInteresse>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                AreaInteresse ai = extractAreaInteresse(rSet);
                areeInteresse.add(ai);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<AreaInteresse>>(ResponseType.Error, areeInteresse);}
        if(areeInteresse.isEmpty()) return new Pair<ResponseType, List<AreaInteresse>>(ResponseType.NoSuchElement, areeInteresse);
        return new Pair<ResponseType, List<AreaInteresse>>(ResponseType.List, areeInteresse);
    }

    private Pair<ResponseType, List<ParametroClimatico>> selectAllPcCond(String fieldCond, String cond){
        String query = "select * from parametro_climatico where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<ParametroClimatico> parametriClimatici = new LinkedList<ParametroClimatico>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                ParametroClimatico cp = extractParametroClimatico(rSet);
                parametriClimatici.add(cp);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<ParametroClimatico>>(ResponseType.Error, parametriClimatici);}
        if(parametriClimatici.isEmpty()) return new Pair<ResponseType, List<ParametroClimatico>>(ResponseType.NoSuchElement, parametriClimatici);
        return new Pair<ResponseType, List<ParametroClimatico>>(ResponseType.List, parametriClimatici);
    }

    private Pair<ResponseType, List<NotaParametro>> selectAllNotaCond(String fieldCond, String cond){
        String query = "select * from nota_parametro_climatico where %s = '%s'".formatted(fieldCond, cond);
        List<NotaParametro> resultList = new LinkedList<NotaParametro>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                NotaParametro np = extractNota(rSet);
                resultList.add(np);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<NotaParametro>>(ResponseType.Error, resultList);}
        if(resultList.isEmpty()) return new Pair<ResponseType, List<NotaParametro>>(ResponseType.NoSuchElement, resultList);
        return new Pair<ResponseType, List<NotaParametro>>(ResponseType.List, resultList);
    }

    private Pair<ResponseType, List<Operatore>> selectAllOpCond(String fieldCond, String cond){
        String query = "select * from operatore where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<Operatore> operatori = new LinkedList<Operatore>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                Operatore op = extractOperatore(rSet);
                operatori.add(op);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<Operatore>>(ResponseType.Error, operatori);}
        if(operatori.isEmpty()) return new Pair<ResponseType, List<Operatore>>(ResponseType.NoSuchElement, operatori);
        return new Pair<ResponseType, List<Operatore>>(ResponseType.List, operatori);
    }

    public Pair<ResponseType, List<OperatoreAutorizzato>> selectAllAuthOpCond(String fieldCond, String cond){
        String query = "select * from operatore_autorizzati where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<OperatoreAutorizzato> opAutorizzati = new LinkedList<OperatoreAutorizzato>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                OperatoreAutorizzato authOp = extractAuthOp(rSet);
                opAutorizzati.add(authOp);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ResponseType, List<OperatoreAutorizzato>>(ResponseType.Error, opAutorizzati);}
        if(opAutorizzati.isEmpty()) return new Pair<ResponseType, List<OperatoreAutorizzato>>(ResponseType.NoSuchElement, opAutorizzati);
        return new Pair<ResponseType, List<OperatoreAutorizzato>>(ResponseType.List, opAutorizzati);
    }

    private Response selectAllWithCond(Request r){
        Map<String, String> params = r.getParams();
        System.out.println("executing request" + r);
        switch(r.getTable()){
            case CITY -> {
                Pair<ResponseType, List<City>> res = selectAllCityCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case CENTRO_MONITORAGGIO -> {
                Pair<ResponseType, List<CentroMonitoraggio>> res = selectAllCmCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case AREA_INTERESSE -> {
                Pair<ResponseType, List<AreaInteresse>> res = selectAllAiCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case PARAM_CLIMATICO -> {
                Pair<ResponseType, List<ParametroClimatico>> res = selectAllPcCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case NOTA_PARAM_CLIMATICO -> {
                Pair<ResponseType, List<NotaParametro>> res = selectAllNotaCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case OPERATORE -> {
                Pair<ResponseType, List<Operatore>> res = selectAllOpCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case OP_AUTORIZZATO -> {
                Pair<ResponseType, List<OperatoreAutorizzato>> res = selectAllAuthOpCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            default -> {
                return new Response(clientId, requestId, responseId, ResponseType.List, r.getTable(), null);
            }
        }
    }
    private Pair<ResponseType, String> getResponseResult(String oggetto, String query) {
        //System.out.println(query);
        String res = getQueryResult(query, oggetto);
        if(res == null) return new Pair<ResponseType, String>(ResponseType.NoSuchElement, "");
        return new Pair<ResponseType, String>(ResponseType.Object, res);
    }

    private Pair<ResponseType, String> selectObjCityCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from city where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjCmCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjAiCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjPcCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjNpcCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from nota_parametro_climatico where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjOpCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from operatore where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ResponseType, String> selectObjAuthOpCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from operatore_autorizzati where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Response selectObjWithCond(Request r){
        Map<String, String> params = r.getParams();
        switch(r.getTable()){
            case CITY -> {
                Pair<ResponseType, String> res = selectObjCityCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.CITY, res.getValue());
            }
            case CENTRO_MONITORAGGIO -> {
                Pair<ResponseType, String> res = selectObjCmCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.CENTRO_MONITORAGGIO, res.getValue());
            }
            case AREA_INTERESSE -> {
                Pair<ResponseType, String> res = selectObjAiCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.AREA_INTERESSE, res.getValue());
            }
            case PARAM_CLIMATICO -> {
                Pair<ResponseType, String> res = selectObjPcCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.PARAM_CLIMATICO, res.getValue());
            }
            case NOTA_PARAM_CLIMATICO -> {
                Pair<ResponseType, String> res = selectObjNpcCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.NOTA_PARAM_CLIMATICO, res.getValue());
            }
            case OPERATORE -> {
                Pair<ResponseType, String> res = selectObjOpCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.OPERATORE, res.getValue());
            }
            case OP_AUTORIZZATO -> {
                Pair<ResponseType, String> res = selectObjAuthOpCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, requestId, responseId, res.getKey(), Tables.OP_AUTORIZZATO, res.getValue());
            }
            default -> {
                return new Response(clientId, requestId, responseId, ResponseType.NoSuchElement, r.getTable(), null);
            }
        }
    }

    private Response getQueryResultList(Tables table, String oggetto, String fieldCond, String cond, String query) {
        query = query.formatted(oggetto, fieldCond, cond);
        List<String> resultList = new LinkedList<String>();
        Response res;
        System.out.println(query);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                resultList.add(rSet.getString(oggetto));
            }
            res = new Response(clientId, requestId, responseId, ResponseType.Object, table, resultList);
        }catch(SQLException sqle){
            sqle.printStackTrace();
            res = new Response(clientId, requestId, responseId, ResponseType.Error, table, resultList);
        }
        if(resultList.isEmpty()) res = new Response(clientId, requestId, responseId, ResponseType.NoSuchElement, table, resultList);
        return res;
    }

    private Response selectObjectCityJoinAiCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from city c join area_interesse ai on c.ascii_name = ai.denominazione where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCityJoinCmCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from city c join centro_monitoraggio cm on c.ascii_name = cm.nomecentro where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCmJoinAiCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio cm join area_interesse ai on ai.areaid = any(cm.aree_interesse_ids) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCmJoinPcCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio cm join parametro_climatico pc using(centroid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectAiJoinPcCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join parametro_climatico pc using(areaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectAiJoinCmCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join centro_monitoraggio cm on ai.areaid = any(aree_interesse_ids) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectAiJoinCityCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join city c on ai.denominazione = c.ascii_name where %s = '%s'" ;
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectNotaJoinPcCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from nota_parametro_climatico npc join parametro_climatico pc using(notaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectPcJoinAiCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join area_interesse ai using(areaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectPcJoinCmCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join centro_monitoraggio using(centroid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectPcJoinNpcCond(Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join nota_parametro_climatico using(notaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    public Response selectObjectJoinWithCond(Request req){
        Map<String, String> params = req.getParams();
        String joinTable = params.get(RequestFactory.joinKey);
        Tables otherTable;
        switch(joinTable){
            case "centro_monitoraggio" -> otherTable = Tables.CENTRO_MONITORAGGIO;
            case "area_interesse"-> otherTable = Tables.AREA_INTERESSE;
            case "city" -> otherTable = Tables.CITY;
            case "parametro_climatico" -> otherTable = Tables.PARAM_CLIMATICO;
            case "operatore" -> otherTable = Tables.OPERATORE;
            case "operatore_autorizzati" -> otherTable = Tables.OP_AUTORIZZATO;
            case "nota_parametro_climatico" -> otherTable = Tables.NOTA_PARAM_CLIMATICO;
            default -> otherTable = null;
        }
        switch(req.getTable()){
            case CITY -> {
                switch(otherTable){
                    case AREA_INTERESSE -> {return selectObjectCityJoinAiCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case CENTRO_MONITORAGGIO -> {return selectObjectCityJoinCmCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                }
            }
            case CENTRO_MONITORAGGIO -> {
                switch(otherTable){
                    case AREA_INTERESSE -> {return selectObjectCmJoinAiCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case PARAM_CLIMATICO -> {return selectObjectCmJoinPcCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                }
            }
            case AREA_INTERESSE -> {
                switch(otherTable){
                    case CENTRO_MONITORAGGIO -> {return selectObjectAiJoinCmCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case PARAM_CLIMATICO -> {return selectObjectAiJoinPcCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case CITY -> {return selectObjectAiJoinCityCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                }
            }
            case NOTA_PARAM_CLIMATICO -> {return selectObjectNotaJoinPcCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
            case PARAM_CLIMATICO -> {
                switch(otherTable){
                    case AREA_INTERESSE -> {return selectObjectPcJoinAiCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case CENTRO_MONITORAGGIO -> {return selectObjectPcJoinCmCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                    case NOTA_PARAM_CLIMATICO -> {return selectObjectPcJoinNpcCond(otherTable, params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));}
                }
            }
            default -> {return new Response(clientId, requestId, responseId, ResponseType.Error, req.getTable(), null);}
        }
        return new Response(clientId, requestId, responseId, ResponseType.Error, req.getTable(), null);
    }

    public Response executeUpdateAi(Request request){
        String areaId  = request.getParams().get(RequestFactory.areaIdKey);
        String centroId = request.getParams().get(RequestFactory.centroIdKey);
        String query = "update centro_monitoraggio set aree_interesse_ids = array_append(aree_interesse_ids, '%s') where centroid = '%s'"
                .formatted(areaId, centroId);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int success = stat.executeUpdate();
            if(success == 1){
                return new Response(clientId, requestId, responseId, ResponseType.updateOk, Tables.CENTRO_MONITORAGGIO, 1);
            }
        }catch(SQLException sqle){
            sqle.printStackTrace();}

        return new Response(clientId, requestId, responseId, ResponseType.updateKo, Tables.CENTRO_MONITORAGGIO, -1);
    }

    public Response executeLogin(Request request){
        String userId = request.getParams().get(RequestFactory.userKey);
        String password = request.getParams().get(RequestFactory.passwordKey);
        String query = "select * from operatore where userid = '%s' and password = '%s'".formatted(userId, password);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            if(rSet.next()){
                Operatore operatore = extractOperatore(rSet);
                return new Response(clientId, requestId, responseId, ResponseType.loginOk, Tables.OPERATORE, operatore);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}

        //Login has failed
        return new Response(clientId, requestId, responseId, ResponseType.loginKo, Tables.OPERATORE, null);
    }

    public Response insertOperatore(Request request){
        Map<String, String> params = request.getParams();
        String nomeOp = params.get(RequestFactory.nomeOpKey);
        String cognomeOp = params.get(RequestFactory.cognomeOpKey);
        String codFisc = params.get(RequestFactory.codFiscOpKey);
        String userId = params.get(RequestFactory.userKey);
        String password = params.get(RequestFactory.passwordKey);
        String email = params.get(RequestFactory.emailOpKey);
        String centroAfferenza = params.get(RequestFactory.centroAfferenzaKey);

        //create role with the userId
        final String CREATE_USER_ROLE = "create role %s with login password '%s'".formatted(userId, password);
        try(PreparedStatement createUserRoleStat = conn.prepareStatement(CREATE_USER_ROLE)){
            boolean res = createUserRoleStat.execute();
        }catch(SQLException sqlException){sqlException.printStackTrace();}

        // add user to group role
        final String ADD_USER_TO_GROUP_ROLE = "grant operatori to %s".formatted(userId);
        System.err.println("Executing: " + ADD_USER_TO_GROUP_ROLE);
        try(PreparedStatement addUserToGroupRoleStat = conn.prepareStatement(ADD_USER_TO_GROUP_ROLE)){
            boolean res = addUserToGroupRoleStat.execute();
        }catch(SQLException sqlException){sqlException.printStackTrace();}

        String query = "insert into operatore(nome, cognome, codice_fiscale, email, userid, password, centroid) values ('%s', '%s', '%s', '%s', '%s', '%s', '%s')"
                .formatted(nomeOp, cognomeOp, codFisc, userId, email, password, centroAfferenza);

        System.err.println(query);

        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1){return new Response( clientId, requestId, responseId, ResponseType.insertOk, Tables.OPERATORE, true);}
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.OPERATORE, false);
    }

    public Response insertOperatoreAutorizzato(Request request){
        Map<String, String> params = request.getParams();
        String email = params.get(RequestFactory.emailOpKey);
        String codFisc = params.get(RequestFactory.codFiscOpKey);
        String query = "insert into operatore_autorizzati(codice_fiscale, email) values('%s','%s')".formatted(codFisc, email);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1){return new Response(clientId, requestId, responseId, ResponseType.insertOk, Tables.OP_AUTORIZZATO, true);}
        }catch (SQLException sqlException){sqlException.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.OP_AUTORIZZATO, false);

    }

    public Response insertCentroMonitoraggio(Request request){
        Map<String, String> params = request.getParams();
        String centroId = IDGenerator.generateID();
        //String,String,String,String
        String[] nomiAree =  params.get(RequestFactory.listAiKey).split(",");
        List<String> idAreeInteresseAssociate = new LinkedList<String>();
        for(String denominazione : nomiAree){
            String query = "select areaid from area_interesse where denominazione = '%s'".formatted(denominazione);
            try(PreparedStatement stat = conn.prepareStatement(query)){
                ResultSet rSet = stat.executeQuery();
                while(rSet.next()){
                    idAreeInteresseAssociate.add(rSet.getString("areaid"));
                }
            }catch(SQLException sqle){sqle.printStackTrace();}
        }
        int idListSize = idAreeInteresseAssociate.size();
        StringBuilder ids = new StringBuilder();
        for(int i = 0; i < idListSize; i++){
            ids.append(idAreeInteresseAssociate.get(i));
            if(i < idListSize - 1)
                ids.append(",");
        }
        String query = "insert into centro_monitoraggio(centroid, nomecentro, comune, country, aree_interesse_ids) values ('%s', '%s', '%s', '%s', '{%s}')"
                .formatted(centroId, params.get(RequestFactory.nomeCentroKey), params.get(RequestFactory.comuneCentroKey), params.get(RequestFactory.countryCentroKey), ids.toString());
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1){
                return new Response(clientId, requestId, responseId, ResponseType.insertOk, Tables.CENTRO_MONITORAGGIO, true);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.CENTRO_MONITORAGGIO, false);
    }

    //Non inserisci notaid
    public Response insertParametroClimatico(Request request){
        Map<String, String> params = request.getParams();
        String parameterId = params.get(RequestFactory.parameterIdKey);
        String centroId = params.get(RequestFactory.centroIdKey);
        String areaId = params.get(RequestFactory.areaIdKey);
        String notaId = params.get(RequestFactory.notaIdKey);
        String pubDate = params.get(RequestFactory.pubDateKey);
        String valoreVento = params.get(RequestFactory.valoreVentoKey);
        String valoreUmidita = params.get(RequestFactory.valoreUmiditaKey);
        String valorePressione = params.get(RequestFactory.valorePressioneKey);
        String valoreTemperatura= params.get(RequestFactory.valoreTemperaturaKey);
        String valorePrecipitazioni = params.get(RequestFactory.valorePrecipitazioniKey);
        String valoreAltGhiacciai = params.get(RequestFactory.valoreAltGhiacciaiKey);
        String valoreMassaGhiacciai = params.get(RequestFactory.valoreMassaGhiacciaiKey);
        String query =
                "insert into parametro_climatico(parameterid, centroid, areaid, pubdate, notaid, valore_vento, valore_umidita, valore_pressione, valore_temperatura, valore_precipitazioni, valore_alt_ghiacciai, valore_massa_ghiacciai) " +
                        "values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";
        //12
        query = query.formatted(parameterId, centroId, areaId, pubDate, notaId, valoreVento, valoreUmidita, valorePressione, valoreTemperatura, valorePrecipitazioni, valoreAltGhiacciai, valoreMassaGhiacciai);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1)
                return new Response(clientId, requestId, responseId, ResponseType.insertOk, Tables.PARAM_CLIMATICO, true);
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.PARAM_CLIMATICO, false);
    }


    private Response insertAreaInteresse(Request request) {
        Map<String, String> params = request.getParams();
        String areaId = params.get(RequestFactory.areaIdKey);
        String denominazione = params.get(RequestFactory.denominazioneAreaKey);
        String stato = params.get(RequestFactory.statoAreaKey);
        String latitudine = params.get(RequestFactory.latitudineKey);
        String longitudine = params.get(RequestFactory.longitudineKey);
        String query = "insert into area_interesse(areaid, denominazione, stato, latitudine, longitudine) values ('%s', '%s', '%s', '%s', '%s')";
        query = query.formatted(areaId, denominazione, stato, latitudine, longitudine);
        //Query the database to check if this ai already exists

        String checkQuery =
                String.format("select areaid from area_interesse where denominazione = '%s' and stato = '%s' and latitudine = '%s' and longitudine = '%s'",
                        denominazione, stato, latitudine, longitudine);
        logger.info(checkQuery);
        try(PreparedStatement checkStat = conn.prepareStatement(checkQuery)){
            ResultSet rSet = checkStat.executeQuery();
            boolean checkResult = rSet.next();
            System.out.println(checkResult);
            if(!checkResult){
                try(PreparedStatement stat = conn.prepareStatement(query)){
                    int res = stat.executeUpdate();
                    logger.info(String.valueOf(res));
                    if(res == 1){
                        return new Response(clientId, requestId, responseId, ResponseType.insertOk, Tables.AREA_INTERESSE, true);
                    }
                }catch(SQLException sqle){sqle.printStackTrace();}
            }else{
                return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.AREA_INTERESSE, ServerInterface.DUPLICATE_ITEM);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.AREA_INTERESSE, false);
    }

    private Response insertNotaParametroClimatico(Request request){
        Map<String, String> params = request.getParams();
        String notaId = params.get(RequestFactory.notaIdKey);
        String notaVento = params.get(RequestFactory.notaVentoKey);
        String notaUmidita = params.get(RequestFactory.notaUmiditaKey);
        String notaPressione = params.get(RequestFactory.notaPressioneKey);
        String notaTemperatura = params.get(RequestFactory.notaTemperaturaKey);
        String notaPrecipitazioni = params.get(RequestFactory.notaPrecipitazioniKey);
        String notaAltGhiacciai = params.get(RequestFactory.notaAltGhiacciaiKey);
        String notaMassaGhiacciai = params.get(RequestFactory.notaMassaGhiacciaiKey);

        String query = "insert into nota_parametro_climatico(notaid, nota_vento, nota_umidita, nota_pressione, nota_temperatura, nota_precipitazioni, nota_alt_ghiacciai, nota_massa_ghiacciai)" +
                "values ('%s','%s', '%s', '%s', '%s', '%s', '%s', '%s')";
        query = query.formatted(notaId, notaVento, notaUmidita, notaPressione, notaTemperatura, notaPrecipitazioni, notaAltGhiacciai, notaMassaGhiacciai);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1){
                return new Response(clientId, requestId, responseId, ResponseType.insertOk, Tables.NOTA_PARAM_CLIMATICO, true);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, requestId, responseId, ResponseType.insertKo, Tables.NOTA_PARAM_CLIMATICO, false);
    }


}
