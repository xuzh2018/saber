package com.xzh.saber.net;

import retrofit2.Response;

/**
 * Created by xzh on 2020/8/30.
 * 公共数据返回类
 */
public class SaberResponse {
    public static <T> SaberResponse create(Response<T> response) {
        SaberResponse saberResponse = new SaberResponse();
        return saberResponse.createSuccessResponse(response);

    }

    private <T> SaberResponse createSuccessResponse(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) {
                return new SaberEmptyResponse();
            } else {
                return new SaberSuccessResponse<>(body);
            }
        } else {
            if (response.errorBody() != null) {
                String msg = response.errorBody().toString();
                return new SaberErrorResponse(msg);
            } else {
                if (response.message() != null) {
                    return new SaberErrorResponse(response.message());
                } else {
                    return new SaberErrorResponse("unknown error");
                }
            }
        }
    }


    public static SaberResponse create(Throwable t) {
        SaberResponse saberResponse = new SaberResponse();
        return saberResponse.createErrorResponse();
    }

    private SaberResponse createErrorResponse() {
        return null;
    }

    private static class SaberEmptyResponse extends SaberResponse {
    }

    private static class SaberErrorResponse extends SaberResponse {
        private final String mError;

        public SaberErrorResponse(String message) {
            this.mError = message;
        }

        public String getmError() {
            return mError;
        }
    }

    private static class SaberSuccessResponse<T> extends SaberResponse {
        private final T body;

        public SaberSuccessResponse(T body) {
            this.body = body;
        }

        public T getBody() {
            return body;
        }
    }
}
