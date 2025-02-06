package com.personal.firebaseapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SmsReciever extends BroadcastReceiver {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = smsMessage.getMessageBody();
                    String senderNumber = smsMessage.getOriginatingAddress();

                    // Inicializar Firebase Realtime Database
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    databaseReference = firebaseDatabase.getReference("sms");

                    // Crear objeto con los datos del mensaje SMS
                    SmsMessageData smsData = new SmsMessageData(senderNumber, messageBody);

                    // Guardar datos en Realtime Database
                    databaseReference.push().setValue(smsData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("SmsReceiver", "Mensaje guardado");
                                } else {
                                    Log.e("SmsReceiver", "Error al guardar mensaje", task.getException());
                                }
                            });

                    // Enviar el mensaje recibido a la actividad SMSActivity
                    Intent smsIntent = new Intent(context, SMSActivity.class);
                    smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    smsIntent.putExtra("sms_body", messageBody);
                    smsIntent.putExtra("sms_sender", senderNumber);
                    context.startActivity(smsIntent);
                }
            }
        }
    }

    // Clase interna para representar los datos del SMS
    public static class SmsMessageData {
        public String sender;
        public String message;

        public SmsMessageData() {
            // Constructor vacío necesario para Firebase
        }

        public SmsMessageData(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }
    }
}
