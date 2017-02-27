/**
 * Copyright 2013 Mani Selvaraj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cysion.tdframework.ex_volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.cysion.tdframework.base.Td;
import com.cysion.tdframework.proxy.ParserProxy;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom implementation of Request<T> class which converts the HttpResponse obtained to Java class objects.
 * Uses GSON library, to parse the response obtained.
 * Ref - JsonRequest<T>
 * @author Mani Selvaraj
 */

public class GsonRequest<T> extends Request<T> {

    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    public static final String PROTOCOL_CONTENT_TYPE =
        String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Listener<T> mListener;

    private Class<T> mJavaClass;
    
    public GsonRequest(int method, String url, Class<T> cls, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        mJavaClass = cls;
        mListener = listener;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    private Map<String, String> mHeaders = new HashMap<String, String>();

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }



    public void setHeader(String title, String content) {
        mHeaders.put(title, content);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            T parsedGSON = Td.getParseProxy().parse(jsonString, mJavaClass);
            return Response.success(parsedGSON,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }


}
