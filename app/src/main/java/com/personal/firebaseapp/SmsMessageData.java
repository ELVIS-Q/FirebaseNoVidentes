package com.personal.firebaseapp;

public class SmsMessageData {
    public String sender;
    public String message;
    public String link;
    public String timestamp;

    // Constructor vacío requerido por Firebase
    public SmsMessageData() {}

    // Constructor para tres parámetros (sin mensaje, se asigna cadena vacía)
    public SmsMessageData(String sender, String link, String timestamp) {
        this.sender = sender;
        this.link = link;
        this.timestamp = timestamp;
        this.message = "";
    }

    // Constructor completo para cuatro parámetros
    public SmsMessageData(String sender, String message, String link, String timestamp) {
        this.sender = sender;
        this.message = message;
        this.link = link;
        this.timestamp = timestamp;
    }
}

