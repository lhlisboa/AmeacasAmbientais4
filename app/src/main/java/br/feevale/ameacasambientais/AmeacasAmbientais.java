package br.feevale.ameacasambientais;

import android.app.Activity;
import android.content.Intent;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.List;

public class AmeacasAmbientais extends Activity {
    ListView listAmeacas;
    riscoAmbiental ameacasAdapter;
    EditText txtDesc;
    EditText txtEnd;
    EditText txtImp;
    AmeacasDBDAO db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_ameacas_ambientais);

        //list view com campos (descricao, endereco e impacto)
        listAmeacas = (ListView) findViewById(R.id.listaAmeacas);
        txtDesc = (EditText) findViewById(R.id.txtDesc);
        txtImp = (EditText) findViewById(R.id.txtImp);
        txtEnd = (EditText) findViewById(R.id.txtEnd);

        db = new AmeacasDBDAO(this);
        db.abrir();

        ameacasAdapter = new riscoAmbiental(this);
        listAmeacas.setAdapter(ameacasAdapter);

        //identifica o item selecionado e o deleta se encontrar
        listAmeacas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ID", "" + id);
                db.deletarAmeaca((int) id);
                ameacasAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    //lista as informações do banco no list view
    public void listaAmeaca(View view) {
        ameacasAdapter = new riscoAmbiental(this);
        listAmeacas.setAdapter(ameacasAdapter);
    }

    //add uma nova ameaca no banco
    public void addAmeaca(View view){
        Ameacas a = new Ameacas();
        a.setDesc(txtDesc.getText().toString());
        a.setEnd(txtEnd.getText().toString());
        a.setImp(txtImp.getText().toString());
        db.adicionarAmeacas(a);
        ameacasAdapter.notifyDataSetChanged();
    }


    /*
    * Começo da configuração do envio por POST
    */

    public void syncOnClick(View view){
        startActivity(new Intent(this, AmeacasDBDAO.class));
    }

    public void EnviarOnClick(View view){
        Thread tr = new Thread(){
            public void run(){
                EditText descricao = (EditText) findViewById(R.id.txtDesc);
                EditText endereco = (EditText) findViewById(R.id.txtEnd);
                EditText impacto = (EditText) findViewById(R.id.txtImp);

                try{
                    final String res;

                    res = enviarPost(descricao.getText().toString(), endereco.getText().toString(), impacto.getText().toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AmeacasAmbientais.this, res, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e){
                    //TODO: handle exception
                }
            }
        };
        tr.start();
    }

    public String enviarPost(String descricao, String endereco, String impacto){
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost("http://10.3.2.245/sincronia.php");
        HttpResponse response = null;

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);
            params.add(new BasicNameValuePair("endereco", endereco));
            params.add(new BasicNameValuePair("descricao", descricao));
            params.add(new BasicNameValuePair("impacto", impacto));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost, localContext);
        }catch (Exception e){
            //TODO: handle exception
        }
        return response.toString();
    }

}
