package com.rafo.hall.service.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public abstract class HttpCallBackHandler
  implements FutureCallback<HttpResponse>
{
  protected abstract boolean process(JSONObject paramJSONObject);

  public void completed(HttpResponse response)
  {
    try
    {
      int status = response.getStatusLine().getStatusCode();
      if ((status >= 200) && (status < 300)) {
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        if ((result == null) || (result.isEmpty())) {
          System.out.println("HttpCallBackHandler completed, but HttpEntity to String is null!");
          return;
        }
        JSONObject jsonret = JSONObject.fromObject(result);
        process(jsonret);
      } else {
        throw new ClientProtocolException("Unexpected http response status: " + status);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  public void failed(Exception ex)
  {
    System.out.println("http response failed:" + ex + ";thread:" + Thread.currentThread().getId());
  }

  public void cancelled()
  {
    System.out.println("http response cancelled;thread:" + Thread.currentThread().getId());
  }
}