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
import it.uninsubria.util.IDGenerator;
import javafx.util.Pair;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class CallableQuery implements Callable<Response> {
    private String callableQueryId;
    private String clientId;
    private String responseId;
    private Request request;
    private Connection conn;
    public CallableQuery(Request request, Properties props){
        this.request = request;
        callableQueryId = request.getRequestId();
        clientId = request.getClientId();
        responseId = IDGenerator.generateID();
        try{
            conn = DriverManager.getConnection(ServerCm.dbUrl, props);
        }catch(SQLException sqle){sqle.printStackTrace();}
    }
    @Override
    public Response call() throws Exception{
        Response res = null;
        switch(request.getRequestType()){
            //query the database
            case selectAll -> {
                return selectAll(request);
            }
            case selectAllWithCond -> {
                if(request.getParams().size() < ServerInterface.selectAllWithCondParamsLength){
                    return new Response(clientId, callableQueryId, responseId,  ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return selectAllWithCond(request);

            }
            case selectObjWithCond -> {
                if(request.getParams().size() < ServerInterface.selectObjWithCondParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }
                else return selectObjWithCond(request);

            }
            case selectObjJoinWithCond -> {
                if(request.getParams().size() < ServerInterface.selectObjJoinWithCondParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return selectObjectJoinWithCond(request);
            }
            case executeLogin -> {
                if(request.getParams().size() < ServerInterface.executeLoginParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return executeLogin(request);

            }
            case executeUpdate -> {
                if(request.getParams().size() < ServerInterface.executeUpdateParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return executeUpdate(request);
            }
            case executeDelete -> {
                if(request.getParams().size() < ServerInterface.executeDeleteParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return executeDelete(request);
            }
            case executeSignUp -> {
                if(request.getParams().size() < ServerInterface.executeSignUpParamsLength){
                    return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                }else return insertOperatore(request);
            }
            case insert -> {
                switch(request.getTable()){
                    case AREA_INTERESSE -> {
                        if(request.getParams().size() < ServerInterface.insertAiParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertAreaInteresse(request);
                    }
                    case PARAM_CLIMATICO -> {
                        if(request.getParams().size() < ServerInterface.insertPcParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertParametroClimatico(request);

                    }
                    case OPERATORE -> {
                        if(request.getParams().size() < ServerInterface.insertOpParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertOperatore(request);
                    }
                    case OP_AUTORIZZATO -> {
                        if(request.getParams().size() < ServerInterface.insertAuthOpParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertOperatoreAutorizzato(request);
                    }
                    case NOTA_PARAM_CLIMATICO -> {
                        if(request.getParams().size() < ServerInterface.insertNpcParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertNotaParametroClimatico(request);
                    }
                    case CENTRO_MONITORAGGIO -> {
                        if(request.getParams().size() < ServerInterface.insertCmParamsLength){
                            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
                        }else return insertCentroMonitoraggio(request);
                    }
                }
            }
        }
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
    }

    public ResultSet prepAndExecuteStatement(String query, String arg) throws SQLException {
        PreparedStatement stat = conn.prepareStatement(query);
        System.out.println(callableQueryId + ":"+ stat);
        stat.setString(1, arg);
        return stat.executeQuery();
    }

    private String getQueryResult(String query, String oggetto){
        String result;
        System.out.println(callableQueryId + ":" + query);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, req.getTable(), res);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, ServerInterface.Tables.CENTRO_MONITORAGGIO, res);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, ServerInterface.Tables.AREA_INTERESSE, res);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, ServerInterface.Tables.OPERATORE, res);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, ServerInterface.Tables.OP_AUTORIZZATO, res);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, ServerInterface.Tables.PARAM_CLIMATICO, res);
            }
        }
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, req.getTable(), null);
    }

    private Pair<ServerInterface.ResponseType, List<City>> selectAllCityCond(String fieldCond, String cond){
        String query = "select * from city where " + fieldCond + " = ?";
        LinkedList<City> cities = new LinkedList<City>();
        try(ResultSet rSet = prepAndExecuteStatement(query, cond)){
            while(rSet.next()){
                City c = extractCity(rSet);
                cities.add(c);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<City>>(ServerInterface.ResponseType.Error, cities);}
        if(cities.isEmpty()) return new Pair<ServerInterface.ResponseType, List<City>>(ServerInterface.ResponseType.NoSuchElement, cities);
        return new Pair<ServerInterface.ResponseType, List<City>>(ServerInterface.ResponseType.List, cities);
    }

    private Pair<ServerInterface.ResponseType, List<CentroMonitoraggio>> selectAllCmCond(String fieldCond, String cond){
        String query = "select * from centro_monitoraggio where " + fieldCond + " = ?";
        List<CentroMonitoraggio> cms = new LinkedList<CentroMonitoraggio>();
        try(ResultSet rSet = prepAndExecuteStatement(query, cond)){
            while(rSet.next()){
                CentroMonitoraggio cm = extractCentroMonitoraggio(rSet);
                cms.add(cm);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<CentroMonitoraggio>>(ServerInterface.ResponseType.Error, cms);}
        if(cms.isEmpty()) return new Pair<ServerInterface.ResponseType, List<CentroMonitoraggio>>(ServerInterface.ResponseType.NoSuchElement, cms);
        return new Pair<ServerInterface.ResponseType, List<CentroMonitoraggio>>(ServerInterface.ResponseType.List, cms);
    }

    private Pair<ServerInterface.ResponseType, List<AreaInteresse>> selectAllAiCond(String fieldCond, String cond){
        String query = "select * from area_interesse where %s = '%s'".formatted(fieldCond, cond);
        System.out.println(query);
        List<AreaInteresse> areeInteresse = new LinkedList<AreaInteresse>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                AreaInteresse ai = extractAreaInteresse(rSet);
                areeInteresse.add(ai);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<AreaInteresse>>(ServerInterface.ResponseType.Error, areeInteresse);}
        if(areeInteresse.isEmpty()) return new Pair<ServerInterface.ResponseType, List<AreaInteresse>>(ServerInterface.ResponseType.NoSuchElement, areeInteresse);
        return new Pair<ServerInterface.ResponseType, List<AreaInteresse>>(ServerInterface.ResponseType.List, areeInteresse);
    }

    private Pair<ServerInterface.ResponseType, List<ParametroClimatico>> selectAllPcCond(String fieldCond, String cond){
        String query = "select * from parametro_climatico where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<ParametroClimatico> parametriClimatici = new LinkedList<ParametroClimatico>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                ParametroClimatico cp = extractParametroClimatico(rSet);
                parametriClimatici.add(cp);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<ParametroClimatico>>(ServerInterface.ResponseType.Error, parametriClimatici);}
        if(parametriClimatici.isEmpty()) return new Pair<ServerInterface.ResponseType, List<ParametroClimatico>>(ServerInterface.ResponseType.NoSuchElement, parametriClimatici);
        return new Pair<ServerInterface.ResponseType, List<ParametroClimatico>>(ServerInterface.ResponseType.List, parametriClimatici);
    }

    private Pair<ServerInterface.ResponseType, List<NotaParametro>> selectAllNotaCond(String fieldCond, String cond){
        String query = "select * from nota_parametro_climatico where %s = '%s'".formatted(fieldCond, cond);
        List<NotaParametro> resultList = new LinkedList<NotaParametro>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                NotaParametro np = extractNota(rSet);
                resultList.add(np);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<NotaParametro>>(ServerInterface.ResponseType.Error, resultList);}
        if(resultList.isEmpty()) return new Pair<ServerInterface.ResponseType, List<NotaParametro>>(ServerInterface.ResponseType.NoSuchElement, resultList);
        return new Pair<ServerInterface.ResponseType, List<NotaParametro>>(ServerInterface.ResponseType.List, resultList);
    }

    private Pair<ServerInterface.ResponseType, List<Operatore>> selectAllOpCond(String fieldCond, String cond){
        String query = "select * from operatore where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<Operatore> operatori = new LinkedList<Operatore>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                Operatore op = extractOperatore(rSet);
                operatori.add(op);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<Operatore>>(ServerInterface.ResponseType.Error, operatori);}
        if(operatori.isEmpty()) return new Pair<ServerInterface.ResponseType, List<Operatore>>(ServerInterface.ResponseType.NoSuchElement, operatori);
        return new Pair<ServerInterface.ResponseType, List<Operatore>>(ServerInterface.ResponseType.List, operatori);
    }

    public Pair<ServerInterface.ResponseType, List<OperatoreAutorizzato>> selectAllAuthOpCond(String fieldCond, String cond){
        String query = "select * from operatore_autorizzati where %s = '%s'".formatted(fieldCond, cond);
        LinkedList<OperatoreAutorizzato> opAutorizzati = new LinkedList<OperatoreAutorizzato>();
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                OperatoreAutorizzato authOp = extractAuthOp(rSet);
                opAutorizzati.add(authOp);
            }
        }catch(SQLException sqle){sqle.printStackTrace(); return new Pair<ServerInterface.ResponseType, List<OperatoreAutorizzato>>(ServerInterface.ResponseType.Error, opAutorizzati);}
        if(opAutorizzati.isEmpty()) return new Pair<ServerInterface.ResponseType, List<OperatoreAutorizzato>>(ServerInterface.ResponseType.NoSuchElement, opAutorizzati);
        return new Pair<ServerInterface.ResponseType, List<OperatoreAutorizzato>>(ServerInterface.ResponseType.List, opAutorizzati);
    }

    private Response selectAllWithCond(Request r){
        Map<String, String> params = r.getParams();
        System.out.println("executing request" + r);
        switch(r.getTable()){
            case CITY -> {
                Pair<ServerInterface.ResponseType, List<City>> res = selectAllCityCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case CENTRO_MONITORAGGIO -> {
                Pair<ServerInterface.ResponseType, List<CentroMonitoraggio>> res = selectAllCmCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case AREA_INTERESSE -> {
                Pair<ServerInterface.ResponseType, List<AreaInteresse>> res = selectAllAiCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case PARAM_CLIMATICO -> {
                Pair<ServerInterface.ResponseType, List<ParametroClimatico>> res = selectAllPcCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case NOTA_PARAM_CLIMATICO -> {
                Pair<ServerInterface.ResponseType, List<NotaParametro>> res = selectAllNotaCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case OPERATORE -> {
                Pair<ServerInterface.ResponseType, List<Operatore>> res = selectAllOpCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            case OP_AUTORIZZATO -> {
                Pair<ServerInterface.ResponseType, List<OperatoreAutorizzato>> res = selectAllAuthOpCond(params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), r.getTable(), res.getValue());
            }
            default -> {
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.List, r.getTable(), null);
            }
        }
    }
    private Pair<ServerInterface.ResponseType, String> getResponseResult(String oggetto, String query) {
        //System.out.println(query);
        String res = getQueryResult(query, oggetto);
        if(res == null) return new Pair<ServerInterface.ResponseType, String>(ServerInterface.ResponseType.NoSuchElement, "");
        return new Pair<ServerInterface.ResponseType, String>(ServerInterface.ResponseType.Object, res);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjCityCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from city where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjCmCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjAiCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjPcCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjNpcCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from nota_parametro_climatico where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjOpCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from operatore where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Pair<ServerInterface.ResponseType, String> selectObjAuthOpCond(String oggetto, String fieldCond, String cond){
        String query = "select %s from operatore_autorizzati where %s = '%s'".formatted(oggetto, fieldCond, cond);
        return getResponseResult(oggetto, query);
    }

    private Response selectObjWithCond(Request r){
        Map<String, String> params = r.getParams();
        switch(r.getTable()){
            case CITY -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjCityCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.CITY, res.getValue());
            }
            case CENTRO_MONITORAGGIO -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjCmCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.CENTRO_MONITORAGGIO, res.getValue());
            }
            case AREA_INTERESSE -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjAiCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.AREA_INTERESSE, res.getValue());
            }
            case PARAM_CLIMATICO -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjPcCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.PARAM_CLIMATICO, res.getValue());
            }
            case NOTA_PARAM_CLIMATICO -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjNpcCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.NOTA_PARAM_CLIMATICO, res.getValue());
            }
            case OPERATORE -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjOpCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.OPERATORE, res.getValue());
            }
            case OP_AUTORIZZATO -> {
                Pair<ServerInterface.ResponseType, String> res = selectObjAuthOpCond(params.get(RequestFactory.objectKey), params.get(RequestFactory.condKey), params.get(RequestFactory.fieldKey));
                return new Response(clientId, callableQueryId, responseId, res.getKey(), ServerInterface.Tables.OP_AUTORIZZATO, res.getValue());
            }
            default -> {
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.NoSuchElement, r.getTable(), null);
            }
        }
    }

    private Response getQueryResultList(ServerInterface.Tables table, String oggetto, String fieldCond, String cond, String query) {
        query = query.formatted(oggetto, fieldCond, cond);
        List<String> resultList = new LinkedList<String>();
        Response res;
        System.out.println(query);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            while(rSet.next()){
                resultList.add(rSet.getString(oggetto));
            }
            res = new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Object, table, resultList);
        }catch(SQLException sqle){
            sqle.printStackTrace();
            res = new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, table, resultList);
        }
        if(resultList.isEmpty()) res = new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.NoSuchElement, table, resultList);
        return res;
    }

    private Response selectObjectCityJoinAiCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from city c join area_interesse ai on c.ascii_name = ai.denominazione where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCityJoinCmCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from city c join centro_monitoraggio cm on c.ascii_name = cm.nomecentro where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCmJoinAiCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio cm join area_interesse ai on ai.areaid = any(cm.aree_interesse_ids) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectCmJoinPcCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from centro_monitoraggio cm join parametro_climatico pc using(centroid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectAiJoinPcCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join parametro_climatico pc using(areaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectAiJoinCmCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join centro_monitoraggio cm on ai.areaid = any(aree_interesse_ids) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectAiJoinCityCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from area_interesse ai join city c on ai.denominazione = c.ascii_name where %s = '%s'" ;
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectNotaJoinPcCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from nota_parametro_climatico npc join parametro_climatico pc using(notaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }


    private Response selectObjectPcJoinAiCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join area_interesse ai using(areaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectPcJoinCmCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join centro_monitoraggio using(centroid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    private Response selectObjectPcJoinNpcCond(ServerInterface.Tables table, String oggetto, String fieldCond, String cond){
        String query = "select %s from parametro_climatico pc join nota_parametro_climatico using(notaid) where %s = '%s'";
        return getQueryResultList(table, oggetto, fieldCond, cond, query);
    }

    public Response selectObjectJoinWithCond(Request req){
        Map<String, String> params = req.getParams();
        String joinTable = params.get(RequestFactory.joinKey);
        ServerInterface.Tables otherTable;
        switch(joinTable){
            case "centro_monitoraggio" -> otherTable = ServerInterface.Tables.CENTRO_MONITORAGGIO;
            case "area_interesse"-> otherTable = ServerInterface.Tables.AREA_INTERESSE;
            case "city" -> otherTable = ServerInterface.Tables.CITY;
            case "parametro_climatico" -> otherTable = ServerInterface.Tables.PARAM_CLIMATICO;
            case "operatore" -> otherTable = ServerInterface.Tables.OPERATORE;
            case "operatore_autorizzati" -> otherTable = ServerInterface.Tables.OP_AUTORIZZATO;
            case "nota_parametro_climatico" -> otherTable = ServerInterface.Tables.NOTA_PARAM_CLIMATICO;
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
            default -> {return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, req.getTable(), null);}
        }
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, req.getTable(), null);
    }

    public Response executeUpdate(Request request){
        switch(request.getTable()){
            case AREA_INTERESSE -> {
                return executeUpdateAi(request);
            }
            case CENTRO_MONITORAGGIO -> {
                return executeUpdateCm(request);
            }
            case PARAM_CLIMATICO -> {
                return executeUpdatePc(request);
            }
            case NOTA_PARAM_CLIMATICO -> {
                return executeUpdateNpc(request);
            }
            case OPERATORE -> {
                return executeUpdateOp(request);
            }
            default -> {
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
            }
        }
    }

    private Response executeUpdateQuery(String updateQuery){
        try(PreparedStatement stat = conn.prepareStatement(updateQuery)){
            int result = stat.executeUpdate();
            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.updateOk, request.getTable(), result);
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.updateKo, request.getTable(), null);
    }
    public Response executeUpdateAi(Request request){
        Map<String, String> params = request.getParams();
        String columnToUpdate = params.get(RequestFactory.columnToUpdateKey);
        String value = params.get(RequestFactory.updateValueKey);
        String areaId  = params.get(RequestFactory.objectIdKey);
        String updateQuery = "update area_interesse set %s = '%s' where areaid = '%s'"
                .formatted(columnToUpdate, value, areaId);
        return executeUpdateQuery(updateQuery);
    }

    public Response executeUpdateCm(Request request){
        Map<String, String> params = request.getParams();
        String columnToUpdate = params.get(RequestFactory.columnToUpdateKey);
        String value = params.get(RequestFactory.updateValueKey);
        String centroId = params.get(RequestFactory.objectIdKey);
        String updateQuery = "update centro_monitoraggio set %s = '%s' where centroid = '%s'"
                .formatted(columnToUpdate, value, centroId);
        return executeUpdateQuery(updateQuery);
    }

    public Response executeUpdateNpc(Request request){
        Map<String, String> params = request.getParams();
        String columnToUpdate = params.get(RequestFactory.columnToUpdateKey);
        String value = params.get(RequestFactory.updateValueKey);
        String notaId = params.get(RequestFactory.objectIdKey);
        String updateQuery = "update nota_parametro_climatico set %s = '%s' where notaid = '%s'"
                .formatted(columnToUpdate, value, notaId);
        return executeUpdateQuery(updateQuery);
    }

    public Response executeUpdateOp(Request request){
        Map<String, String> params = request.getParams();
        String columnToUpdate = params.get(RequestFactory.columnToUpdateKey);
        String value = params.get(RequestFactory.updateValueKey);
        String codiceFiscale = params.get(RequestFactory.objectIdKey);
        String updateQuery = "update operatore set %s = '%s' where codice_fiscale = '%s'"
                .formatted(columnToUpdate, value, codiceFiscale);
        return executeUpdateQuery(updateQuery);
    }

    public Response executeUpdatePc(Request request){
        Map<String, String> params = request.getParams();
        String parametroId = params.get(RequestFactory.objectIdKey);
        String columnToUpdate = params.get(RequestFactory.columnToUpdateKey);
        String value = params.get(RequestFactory.updateValueKey);
        String updateQuery = "update parametro_climatico set %s = '%s' where parameterid = '%s'"
                .formatted(columnToUpdate, value, parametroId);
        return executeUpdateQuery(updateQuery);
    }

    private Response executeDeleteQuery(String deleteQuery){
        try(PreparedStatement stat = conn.prepareStatement(deleteQuery)){
            int result = stat.executeUpdate();
            return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.deleteOk, request.getTable(), result);
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.deleteKo, request.getTable(), null);
    }
    public Response executeDeleteAi(Request request){
        Map<String, String> params = request.getParams();
        String areaId = params.get(RequestFactory.objectIdKey);
        String deleteQuery = "delete from area_interesse where areaid = '%s'".formatted(areaId);
        return executeDeleteQuery(deleteQuery);
    }
    public Response executeDeleteCm(Request request){
        Map<String, String> params = request.getParams();
        String centroId = params.get(RequestFactory.objectIdKey);
        String deleteQuery = "delete from centro_monitoraggio where centroid = '%s'".formatted(centroId);
        return executeDeleteQuery(deleteQuery);
    }
    public Response executeDeletePc(Request request){
        Map<String, String> params = request.getParams();
        String parameterId = params.get(RequestFactory.objectIdKey);
        String deleteQuery = "delete from parametro_climatico where parameterid = '%s'".formatted(parameterId);
        return executeDeleteQuery(deleteQuery);
    }
    public Response executeDeleteNpc(Request request){
        Map<String, String> params = request.getParams();
        String notaId = params.get(RequestFactory.objectIdKey);
        String deleteQuery = "delete from nota_parametro_climatico where notaid = '%s'".formatted(notaId);
        return executeDeleteQuery(deleteQuery);
    }
    public Response executeDeleteOp(Request request){
        Map<String, String> params = request.getParams();
        String codiceFiscale = params.get(RequestFactory.objectIdKey);
        String deleteQuery = "delete from operatore where codice_fiscale = '%s'".formatted(codiceFiscale);
        return executeDeleteQuery(deleteQuery);
    }

    public Response executeDelete(Request request){
        switch(request.getTable()){
            case AREA_INTERESSE -> {
                return executeDeleteAi(request);
            }
            case CENTRO_MONITORAGGIO -> {
                return executeDeleteCm(request);
            }
            case PARAM_CLIMATICO -> {
                return executeDeletePc(request);
            }
            case NOTA_PARAM_CLIMATICO -> {
                return executeDeleteNpc(request);
            }
            case OPERATORE -> {
                return executeDeleteOp(request);
            }
            default -> {
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.Error, request.getTable(), null);
            }
        }
    }

    public Response executeLogin(Request request){
        String userId = request.getParams().get(RequestFactory.userKey);
        String password = request.getParams().get(RequestFactory.passwordKey);
        String query = "select * from operatore where userid = '%s' and password = '%s'".formatted(userId, password);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            ResultSet rSet = stat.executeQuery();
            if(rSet.next()){
                Operatore operatore = extractOperatore(rSet);
                Response res = new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.loginOk, ServerInterface.Tables.OPERATORE, operatore);
                System.out.println(res);
                return res;
            }
        }catch(SQLException sqle){sqle.printStackTrace();}

        //Login has failed
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.loginKo, ServerInterface.Tables.OPERATORE, null);
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
            if(res == 1){return new Response( clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.OPERATORE, true);}
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.OPERATORE, false);
    }

    public Response insertOperatoreAutorizzato(Request request){
        Map<String, String> params = request.getParams();
        String email = params.get(RequestFactory.emailOpKey);
        String codFisc = params.get(RequestFactory.codFiscOpKey);
        String query = "insert into operatore_autorizzati(codice_fiscale, email) values('%s','%s')".formatted(codFisc, email);
        try(PreparedStatement stat = conn.prepareStatement(query)){
            int res = stat.executeUpdate();
            if(res == 1){return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.OP_AUTORIZZATO, true);}
        }catch (SQLException sqlException){sqlException.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.OP_AUTORIZZATO, false);

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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.CENTRO_MONITORAGGIO, true);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.CENTRO_MONITORAGGIO, false);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.PARAM_CLIMATICO, true);
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.PARAM_CLIMATICO, false);
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
        try(PreparedStatement checkStat = conn.prepareStatement(checkQuery)){
            ResultSet rSet = checkStat.executeQuery();
            boolean checkResult = rSet.next();
            System.out.println(checkResult);
            if(!checkResult){
                try(PreparedStatement stat = conn.prepareStatement(query)){
                    int res = stat.executeUpdate();
                    if(res == 1){
                        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.AREA_INTERESSE, true);
                    }
                }catch(SQLException sqle){sqle.printStackTrace();}
            }else{
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.AREA_INTERESSE, ServerInterface.DUPLICATE_ITEM);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.AREA_INTERESSE, false);
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
                return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertOk, ServerInterface.Tables.NOTA_PARAM_CLIMATICO, true);
            }
        }catch(SQLException sqle){sqle.printStackTrace();}
        return new Response(clientId, callableQueryId, responseId, ServerInterface.ResponseType.insertKo, ServerInterface.Tables.NOTA_PARAM_CLIMATICO, false);
    }
}
