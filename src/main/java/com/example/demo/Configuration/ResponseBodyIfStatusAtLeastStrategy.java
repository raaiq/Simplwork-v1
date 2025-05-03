package com.example.demo.Configuration;

import org.zalando.logbook.*;

import java.io.IOException;
import java.util.Objects;

//TODO:Move class to proper package
public class ResponseBodyIfStatusAtLeastStrategy implements Strategy {
    private final int status;

    public void write(final Precorrelation precorrelation, final HttpRequest request, final Sink sink) {
    }
    //TODO: Check if it's necessary and safe to log request body
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response, final Sink sink) throws IOException {

            sink.writeBoth(correlation, request.withoutBody(), response.withoutBody());

    }

    public ResponseBodyIfStatusAtLeastStrategy(final int status) {
        this.status = status;
    }
}
