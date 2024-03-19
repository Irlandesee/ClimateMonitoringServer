package it.uninsubria.servercm;

import it.uninsubria.request.Request;
import it.uninsubria.response.Response;

import java.io.IOException;

public interface ServerInterface {

     enum Tables{
        AREA_INTERESSE("area_interesse"),
        CENTRO_MONITORAGGIO("centro_monitoraggio"),
        CITY("city"),
        NOTA_PARAM_CLIMATICO("nota_parametro_climatico"),
        OPERATORE("operatore"),
        OP_AUTORIZZATO("operatore_autorizzati"),
        PARAM_CLIMATICO("parametro_climatico");

        public final String label;
        private Tables(String label){
            this.label = label;
        }
    }

    enum RequestType{
        selectAll("selectAll"),
        selectAllWithCond("selectAllWithCond"),
        selectObjWithCond("selectObjectWithCond"),
        selectObjJoinWithCond("selectObjectJoinWithCond"),
        executeLogin("executeLogin"),
        insert("insert"),
        delete("delete"),
        update("update"),
        requestSignUp("requestSignUp"),
        executeSignUp("executeSignUp");
        public final String label;
        private RequestType(String label){
            this.label = label;
        }
    }

    enum ResponseType {
        List("List"),
        Object("Object"),
        NoSuchElement("NoSuchElement"),
        Error("Error"),
        insertOk("insertOk"),
        insertKo("insertKo"),
        loginOk("loginOk"),
        loginKo("loginKo"),
        updateOk("updateOk"),
        updateKo("updateKo"),
        deleteOk("deleteOk"),
        deleteKo("deleteKo"),
        requestSignUpOk("requestSignUpOk"),
        requestSignUpKo("requestSignUpKo"),
        executeSignUpOk("executeSignUpOk"),
        executeSignUpKo("executeSignUpKo");
        public final String label;
        private ResponseType(String label){this.label = label;}
    }

    int PORT = 9999;
    int selectAllWithCondParamsLength = 2;
    int selectObjWithCondParamsLength = 3;
    int selectObjJoinWithCondParamsLength = 4;
    int executeLoginParamsLength = 2;
    int executeSignUpParamsLength = 7;
    int insertPcParamsLength = 12;
    int insertCmParamsLength = 4;
    int insertAuthOpParamsLength = 2;
    int insertOpParamsLength = 7;
    int insertAiParamsLength = 5;
    int insertNpcParamsLength = 7;
    int executeUpdateParamsLength = 2;
    int executeDeleteParamsLength = 3;

    //int insertAiParamsLength ;

    String TEST = "TEST";
    String QUIT = "QUIT";
    String NEXT = "NEXT";
    String ID = "ID";
    String UNDEFINED_BEHAVIOUR = "UNDEFINED";
    String SUCCESSFULL_INSERT = "SUCCESSFUL_INSERT";
    String UNSUCCESSFULL_INSERT = "UNSUCCESSFUL_INSERT";
    String DUPLICATE_ITEM = "DUPLICATE_ITEM";

    //public void quit() throws IOException;
    void sendRequest(Request r);


}
