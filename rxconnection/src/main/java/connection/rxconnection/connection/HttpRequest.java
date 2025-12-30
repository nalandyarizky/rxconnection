package connection.rxconnection.connection;

import android.content.Context;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.Map;

import connection.rxconnection.model.BaseResponse;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class HttpRequest<REQUEST, RESPONSE> implements CallBackOKHttp, Observable.OnSubscribe<BaseResponse<RESPONSE>> {
    @Getter
    private final Context context;
    private final String url;
    @Getter
    private REQUEST request;
    @Getter
    private OKHttpConnection<REQUEST, RESPONSE> teokHttpConnection;
    private Class<RESPONSE> eClass;
    private int httpMethod;
    private Subscriber<? super BaseResponse<RESPONSE>> subscriber;
    private Map<String, String> customHeader;
    @Getter
    private boolean multipart;
    private boolean logInfoRequestResponse;
    private boolean downloadFile;
    private File fileDownload;
    private int connectionTimeout = 1;
    private int readTimeout = 1;
    private int writeTimeout = 1;
    private MediaType mediaTypeResponse;
    private MediaType mediatypeRequest;
    private ProgressDownloadListener progressDownloadListener;
    private CallBackForLog callBackForLog;
    @Getter
    private String message;
    private Proxy proxy;


    public HttpRequest(REQUEST request, Context context, Class<RESPONSE> resultClass, String url,
                       int httpMethod) {
//        super(f);
        this.request = request;
        this.context = context;
        this.eClass = resultClass;
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaTypeResponse = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }


    public HttpRequest(Context context, Class<RESPONSE> resultClass, String url, int httpMethod) {
//        super(f);
        this.context = context;
        this.eClass = resultClass;
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaTypeResponse = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }


    public HttpRequest(REQUEST request, Context context, String url,
                       int httpMethod) {
//        super(f);
        this.request = request;
        this.context = context;
        this.eClass = makeResultClass();
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaTypeResponse = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }


    public HttpRequest(Context context, String url, File fileDownload, ProgressDownloadListener progressDownloadListener) {
//        super(f);
        this.progressDownloadListener = progressDownloadListener;
        this.fileDownload = fileDownload;
        downloadFile = true;
        this.context = context;
        this.url = url;
        teokHttpConnection = new OKHttpConnection(this);
    }

    public HttpRequest(Context context, String url, int httpMethod) {
//        super(f);
        this.context = context;
        this.eClass = makeResultClass();
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaTypeResponse = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }

    private Class<RESPONSE> makeResultClass() {
        ParameterizedType parameterizedType = ((ParameterizedType) getClass()
                .getGenericSuperclass());
        Type[] type = parameterizedType.getActualTypeArguments();
        return ((Class<RESPONSE>) type[1]);
    }

    public HttpRequest<REQUEST, RESPONSE> setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setCallBackForLog(CallBackForLog callBackForLog) {
        this.callBackForLog = callBackForLog;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMessage(String message) {
        this.message = message;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setLogInfoRequestResponse(boolean logInfoRequestResponse) {
        this.logInfoRequestResponse = logInfoRequestResponse;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaTypeResponse(MediaType mediaType) {
        this.mediaTypeResponse = mediaType;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaTypeRequest(MediaType mediaType) {
        this.mediatypeRequest = mediaType;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setCustomHeader(Map<String, String> customHeader) {
        this.customHeader = customHeader;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMultipart(boolean multipart) {
        this.multipart = multipart;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }


    @Override
    public void call(Subscriber<? super BaseResponse<RESPONSE>> subscriber) {
        this.subscriber = subscriber;
        teokHttpConnection.setCustomHeader(customHeader);
        if (downloadFile) {
            teokHttpConnection.download(url, fileDownload, progressDownloadListener, context);
        } else {
            teokHttpConnection.setConnectionTimeOut(connectionTimeout);
            teokHttpConnection.setReadTimeOut(readTimeout);
            teokHttpConnection.setWriteTimeOut(writeTimeout);
            teokHttpConnection.setMultipart(multipart);
            teokHttpConnection.setLogInfoRequestResponse(logInfoRequestResponse);
            teokHttpConnection.setCallBackForLog(callBackForLog);
            teokHttpConnection.setProxy(proxy);
            teokHttpConnection.data(request, url, eClass, httpMethod, mediatypeRequest, mediaTypeResponse, context);
        }
    }

    @Override
    public void error(ExceptionHttpRequest exceptionHttpRequest) {
        exceptionHttpRequest.setHttpRequest(this);
        subscriber.onError(exceptionHttpRequest);
    }

    @Override
    public <T> void success(T t) {
        BaseResponse<RESPONSE> response = new BaseResponse<>();
        try {
            response = (BaseResponse<RESPONSE>) t;
        } catch (Exception e) {
            e.printStackTrace();
            response = new BaseResponse<>();
            response.setError(e.getMessage());
        }

        subscriber.onNext(response);
    }

    @Override
    public void doneDownload() {
        subscriber.onCompleted();
    }

    public OkHttpClient getOkhttpClient() {
        return teokHttpConnection.getOkHttpClient();
    }

}
