package it.uninsubria.factories;

import it.uninsubria.request.MalformedRequestException;
import it.uninsubria.request.Request;
import it.uninsubria.servercm.ServerInterface;

import java.util.HashMap;
import java.util.Map;

public class RequestFactory {

    static final String paramLengthError = "Lunghezza parametri non corretta";
    static final String undefinedRequestType = "Tipo richiesta non definito";
    public static final String condKey = "cond";
    public static final String fieldKey = "field";
    public static final String columnKey = "column";
    public static final String objectId = "objectId";
    public static final String objectKey = "object";
    public static final String updateValueKey = "updateValue";
    public static final String joinKey = "joinTable";
    public static final String userKey = "user";
    public static final String passwordKey = "password";
    public static final String nomeOpKey = "nomeOp";
    public static final String cognomeOpKey = "cognomeOp";
    public static final String codFiscOpKey = "codFiscOp";
    public static final String emailOpKey = "emailOp";
    public static final String centroAfferenzaKey = "centroAfferenzaOp";
    public static final String parameterIdKey = "parameterId";
    public static final String centroIdKey = "centroId";
    public static final String areaIdKey = "areaId";
    public static final String pubDateKey = "pubdate";
    public static final String notaIdKey = "notaid";
    public static final String valoreVentoKey = "valore_vento";
    public static final String valoreUmiditaKey = "valore_umidita";
    public static final String valorePressioneKey = "valore_pressione";
    public static final String valoreTemperaturaKey = "valore_temperatura";
    public static final String valorePrecipitazioniKey = "valore_precipitazioni";
    public static final String valoreAltGhiacciaiKey = "valore_alt_ghiacciai";
    public static final String valoreMassaGhiacciaiKey = "valore_massa_ghiacciai";

    public static final String nomeCentroKey = "nomeCentro";
    public static final String comuneCentroKey = "comuneCentro";
    public static final String countryCentroKey = "countryCentro";
    public static final String listAiKey = "listAi";
    public static final String denominazioneAreaKey = "denominazioneArea";
    public static final String statoAreaKey = "statoArea";
    public static final String latitudineKey = "latitudine";
    public static final String longitudineKey = "longitudine";
    public static final String notaVentoKey = "notaVento";
    public static final String notaUmiditaKey = "notaUmidita";
    public static final String notaPressioneKey = "notaPressione";
    public static final String notaTemperaturaKey = "notaTemperatura";
    public static final String notaPrecipitazioniKey = "notaPrecipitazioni";
    public static final String notaAltGhiacciaiKey = "notaAltGhiacciai";
    public static final String notaMassaGhiacciaiKey = "notaMassaGhiacciai";

    public static Request buildRequest(String clientId, ServerInterface.RequestType requestType, ServerInterface.Tables table, Map<String, String> params) throws MalformedRequestException{
        switch(requestType){
            case selectAll -> {
                return new Request(clientId, requestType, table, null);
            }
            case selectAllWithCond -> {
                if(params.keySet().size() < ServerInterface.selectAllWithCondParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case selectObjWithCond -> {
                if(params.keySet().size() < ServerInterface.selectObjWithCondParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case selectObjJoinWithCond -> {
                if(params.keySet().size() < ServerInterface.selectObjJoinWithCondParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case executeLogin -> {
                if(params.keySet().size() < ServerInterface.executeLoginParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case executeSignUp -> {
                if(params.keySet().size() < ServerInterface.executeSignUpParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case delete -> {
                if(params.keySet().size() < ServerInterface.executeDeleteParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case update ->{
                if(params.keySet().size() < ServerInterface.executeUpdateParamsLength){
                    throw new MalformedRequestException(paramLengthError);
                }else{
                    return new Request(clientId, requestType, table, params);
                }
            }
            case insert -> {
                switch (table){
                    case AREA_INTERESSE -> {
                        if(params.keySet().size() < ServerInterface.insertAiParamsLength){
                            throw new MalformedRequestException(paramLengthError);
                        }else{
                            return new Request(clientId, requestType, table, params);
                        }
                    }
                    case CENTRO_MONITORAGGIO -> {
                        if(params.keySet().size() < ServerInterface.insertCmParamsLength){
                            throw new MalformedRequestException(paramLengthError);
                        }else{
                            return new Request(clientId, requestType, table, params);
                        }
                    }
                    case OPERATORE -> {
                        if(params.keySet().size() < ServerInterface.insertOpParamsLength){
                            throw new MalformedRequestException(paramLengthError);
                        }else{
                            return new Request(clientId, requestType, table, params);
                        }
                    }
                    case PARAM_CLIMATICO -> {
                        if(params.keySet().size() < ServerInterface.insertPcParamsLength){
                            throw new MalformedRequestException(paramLengthError);
                        }else{
                            return new Request(clientId, requestType, table, params);
                        }
                    }
                    case NOTA_PARAM_CLIMATICO -> {
                        if(params.keySet().size() < ServerInterface.insertNpcParamsLength){
                            throw new MalformedRequestException(paramLengthError);
                        }else{
                            return new Request(clientId, requestType, table, params);
                        }

                    }
                }
            }
        }
        throw new MalformedRequestException(undefinedRequestType);
    }

    public static Map<String, String> buildInsertParams(ServerInterface.Tables table, String... s) throws MalformedRequestException{
        Map<String, String> params = new HashMap<String, String>();
        switch(table){
            case AREA_INTERESSE -> {
                if(s.length < 5) throw new MalformedRequestException(paramLengthError);
                params.put(areaIdKey, s[0]);
                params.put(denominazioneAreaKey, s[1]);
                params.put(statoAreaKey, s[2]);
                params.put(latitudineKey, s[3]);
                params.put(longitudineKey, s[4]);
            }
            case PARAM_CLIMATICO -> {
                if(s.length < 12) throw new MalformedRequestException(paramLengthError);
                params.put(parameterIdKey, s[0]);
                params.put(centroIdKey, s[1]);
                params.put(areaIdKey, s[2]);
                params.put(pubDateKey, s[3]);
                params.put(notaIdKey, s[4]);
                params.put(valoreVentoKey, s[5]);
                params.put(valoreUmiditaKey, s[6]);
                params.put(valorePressioneKey, s[7]);
                params.put(valoreTemperaturaKey, s[8]);
                params.put(valorePrecipitazioniKey, s[9]);
                params.put(valoreAltGhiacciaiKey, s[10]);
                params.put(valoreMassaGhiacciaiKey, s[11]);
            }
            case CENTRO_MONITORAGGIO -> {
                if(s.length < 5) throw new MalformedRequestException(paramLengthError);
                params.put(centroIdKey, s[0]);
                params.put(nomeCentroKey, s[1]);
                params.put(comuneCentroKey, s[2]);
                params.put(countryCentroKey, s[3]);
                params.put(listAiKey, s[4]);
            }
            case NOTA_PARAM_CLIMATICO -> {
                if(s.length < 7) throw new MalformedRequestException(paramLengthError);
                params.put(notaIdKey, s[0]);
                params.put(notaVentoKey, s[1]);
                params.put(notaUmiditaKey, s[2]);
                params.put(notaPressioneKey, s[3]);
                params.put(notaPrecipitazioniKey, s[4]);
                params.put(notaAltGhiacciaiKey, s[5]);
                params.put(notaMassaGhiacciaiKey, s[6]);
            }
            case OPERATORE -> {
                if(s.length < 7) throw new MalformedRequestException(paramLengthError);
                params.put(nomeOpKey, s[0]);
                params.put(cognomeOpKey, s[1]);
                params.put(codFiscOpKey, s[2]);
                params.put(emailOpKey, s[3]);
                params.put(userKey, s[4]);
                params.put(passwordKey, s[5]);
                params.put(centroAfferenzaKey, s[6]);
            }
            case OP_AUTORIZZATO -> {
                if(s.length < 2) throw new MalformedRequestException(paramLengthError);
                params.put(codFiscOpKey, s[0]);
                params.put(emailOpKey, s[1]);
            }
            default -> {return null;}
        }
        return params;
    }

    public static Map<String, String> buildUpdateParams(ServerInterface.Tables table, ServerInterface.RequestType requestType, String... s) throws MalformedRequestException{
        Map<String, String> params = new HashMap<String, String>();
        params.put(columnKey, s[0]);
        params.put(updateValueKey, s[1]);
        switch(table){
            case AREA_INTERESSE -> params.put(areaIdKey, s[2]);
            case CENTRO_MONITORAGGIO -> params.put(centroIdKey, s[2]);
            case PARAM_CLIMATICO -> params.put(parameterIdKey, s[2]);
            case NOTA_PARAM_CLIMATICO -> params.put(notaIdKey, s[2]);
            case OPERATORE -> params.put(codFiscOpKey, s[2]);
            default -> {return null;}
        }
        return params;
    }
    public static Map<String, String> buildParams(ServerInterface.RequestType requestType, String... s) throws MalformedRequestException{
        Map<String, String> params = new HashMap<String, String>();
        switch(requestType){
            case selectAll -> {
                return params;
            }
            case selectAllWithCond -> {
                if(s.length < 2){
                    throw new MalformedRequestException(paramLengthError);
                }
                params.put(RequestFactory.condKey, s[0]);
                params.put(RequestFactory.fieldKey, s[1]);
            }
            case selectObjWithCond -> {
                if(s.length < 3){
                    throw new MalformedRequestException(paramLengthError);
                }
                params.put(RequestFactory.objectKey, s[0]);
                params.put(RequestFactory.condKey, s[1]);
                params.put(RequestFactory.fieldKey, s[2]);
            }
            case selectObjJoinWithCond -> {
                if(s.length < 4) throw new MalformedRequestException(paramLengthError);
                params.put(RequestFactory.objectKey, s[0]);
                params.put(RequestFactory.joinKey, s[1]);
                params.put(RequestFactory.condKey, s[2]);
                params.put(RequestFactory.fieldKey, s[3]);
            }
            case executeLogin -> {
                if(s.length < 2) throw new MalformedRequestException(paramLengthError);
                params.put(RequestFactory.userKey, s[0]);
                params.put(RequestFactory.passwordKey, s[1]);
            }
            case delete -> {
                if(s.length < 1) throw new MalformedRequestException(paramLengthError);
                params.put(RequestFactory.objectId, s[0]);
            }
            case executeSignUp -> {
                if(s.length < 7) throw new MalformedRequestException(paramLengthError);
                params.put(nomeOpKey, s[0]);
                params.put(cognomeOpKey, s[1]);
                params.put(codFiscOpKey, s[2]);
                params.put(emailOpKey, s[3]);
                params.put(userKey, s[4]);
                params.put(passwordKey, s[5]);
                params.put(centroAfferenzaKey, s[6]);
            }
            default -> {
                return null;
            }
        }

        return params;
    }

}
