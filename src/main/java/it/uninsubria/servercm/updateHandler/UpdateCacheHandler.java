package it.uninsubria.servercm.updateHandler;

import it.uninsubria.factories.RequestFactory;
import it.uninsubria.request.MalformedRequestException;
import it.uninsubria.request.Request;
import it.uninsubria.response.Response;
import it.uninsubria.servercm.CallableQuery;
import it.uninsubria.servercm.ServerInterface;
import it.uninsubria.update.Update;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class UpdateCacheHandler{

    private ConcurrentHashMap<ServerInterface.Tables, Update> updatesCache;
    private final ExecutorService executorService;
    private final String updaterName;
    private final Properties props;
    public UpdateCacheHandler(String updaterName, Properties props){
        this.props = props;
        this.updaterName = updaterName;
        updatesCache = new ConcurrentHashMap<ServerInterface.Tables, Update>();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void update(){
        try{
            Request updateRequest = RequestFactory.buildRequest(
                    updaterName,
                    ServerInterface.RequestType.requestUpdate,
                    ServerInterface.Tables.UPDATE,
                    null);
            CallableQuery query = new CallableQuery(updateRequest, props);
            Future<Response> future = executorService.submit(query);
            try{
                Response updateResponse = future.get();
                if(updateResponse.getResponseType() != ServerInterface.ResponseType.Error){
                    Update update = (Update) updateResponse.getResult();
                    updatesCache.put(update.table(), update);
                }
            }catch(InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        }catch(MalformedRequestException mre){
            mre.printStackTrace();
        }
    }

    public List<Update> getLatestUpdates(){
        update();
        List<Update> updates = new LinkedList<Update>();
        updatesCache.forEach((key, update) -> updates.add(update));
        return updates;
    }


}
