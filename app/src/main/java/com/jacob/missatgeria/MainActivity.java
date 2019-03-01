package com.jacob.missatgeria;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    public static final String NOM_PREFERENCIES = "PreferenciesQuepassaEh";

    Button login;
    TextView user;
    TextView pass;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        login = (Button) findViewById(R.id.login);
        user = (TextView) findViewById(R.id.usuari);
        pass = (TextView) findViewById(R.id.password);
        result = (TextView) findViewById(R.id.resultat);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Executam l'AsyncTask passant-li com a argument la ruta.
                String usuari = user.getText().toString();
                String password = pass.getText().toString();
                String url = "https://iesmantpc.000webhostapp.com/public/login/";
                HashMap<String,String> map = new HashMap<>();
                map.put("nom",usuari);
                map.put("password",password);
                String res = CridadaPost(url,map);

                result.setText(res);

                toJson(res);
            }
        });

    }
    public void toJson(String res){
        try {
            JSONObject jsonObject = new JSONObject(res);
            JSONObject dades = new JSONObject(jsonObject.getString("dades"));

            boolean bo = jsonObject.getBoolean("correcta");
            if (bo) {
                Log.d("RETORNAT-DADES", "toJson: " + dades);
                SharedPreferences.Editor editor = getSharedPreferences(NOM_PREFERENCIES, MODE_PRIVATE).edit();
                editor.putInt("CLAU_CODIUSUARI", dades.getInt("codiusuari"));
                editor.putString("CLAU_USER", dades.getString("nom"));
                editor.putString("CLAU_PASSWD", pass.getText().toString());
                editor.putString("TOKEN", dades.getString("token"));
                editor.apply();

            } else {
                String msgError = jsonObject.getString("rowcount");
                Toast.makeText(this,msgError,Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static String CridadaPost(String adrecaURL,HashMap<String,String> parametres) {
        String resultat="";
        try {
            URL url = new URL(adrecaURL);
            Log.i("ResConnectUtils", "Connectant"+adrecaURL);
            HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
            httpConn.setReadTimeout(15000);
            httpConn.setConnectTimeout(25000);
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
            writer.write(montaParametres(parametres));
            writer.flush();
            writer.close();
            os.close();
            int resposta = httpConn.getResponseCode();
            if (resposta == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new
                        InputStreamReader(httpConn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    resultat+=line;
                }
                Log.i("ResConnectUtils", resultat);
            }
            else {
                resultat="";
                Log.i("ResConnectUtils","Errors:"+resposta);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultat;
    }
    private static String montaParametres(HashMap<String, String> params) throws
            UnsupportedEncodingException {
        // A partir d'un hashmap clau-valor cream
        // clau1=valor1&clau2=valor2&...
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) { first = false;} else {result.append("&");}
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
