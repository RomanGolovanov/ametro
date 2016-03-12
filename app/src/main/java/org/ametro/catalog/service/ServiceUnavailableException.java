package org.ametro.catalog.service;

public class ServiceUnavailableException extends Exception {
    public ServiceUnavailableException(Exception ex)  {
        super(ex);
    }
}
