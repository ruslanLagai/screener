package com.home.project.stocks.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

public class TinkoffServerException extends HttpServerErrorException {

    public TinkoffServerException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
