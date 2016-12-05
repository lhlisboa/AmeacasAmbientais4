package br.feevale.ameacasambientais;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by 0126096 on 01/11/2016.
 */
public class AmeacasDBDAO extends Activity{

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ameacas_ambientais);
        Thread tr = new Thread(){
            @Override
            public void run(){
                final String Resultado = leer();
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                carregaElementos(obtJSON(Resultado));
                            }
                        });
            }

        };
        tr.start();
    }
/*
    public boolean onCreateOptiosMenu(Menu menu){
        getMenuInflater().inflate(R.menu.elementos, menu);
        return true;
    }
*/
    public void carregaElementos(ArrayList<String> dados){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dados);
        ListView elementos = (ListView) findViewById(R.id.listaAmeacas);
        elementos.setAdapter(adapter);
    }

    public String leer(){
        HttpClient cliente = new DefaultHttpClient();
        HttpContext ctx = new BasicHttpContext();
        HttpGet httpget = new HttpGet("http://10.3.2.245/sincronia.php");
        String resultado = null;
        try{
            HttpResponse response = cliente.execute(httpget,ctx);
            HttpEntity entity = response.getEntity();
            resultado = EntityUtils.toString(entity, "UTF-8");
            }catch (Exception e){
            //TODO: handle exception
            }
            return resultado;
    }

    public ArrayList<String> obtJSON(String response){
        ArrayList<String> elementos = new ArrayList<String>();
        try {
            JSONArray json = new JSONArray(response);
            String texto="";
            for (int i=0; i<json.length(); i++){
                texto = json.getJSONObject(i).getString("descricao") + " - " +
                        json.getJSONObject(i).getString("endereco") + " - " +
                        json.getJSONObject(i).getString("impacto");
                elementos.add(texto);
            }
        }catch (Exception e){
            //TODO: handle exception
        }
        return elementos;
    }

    //informações sobre o banco para serem usados no dbhelper
    private static final String NOME_BANCO = "ameacas.db";
    private static final String TAB_AMEACAS = "AMEACAS";
    private static final int VERSAO_BANCO = 1;

    //colunas e seus "apelidos"
    private static final String COL_ID = "ID";
    private static final String COL_DESC = "DESC";
    private static final String COL_END = "END";
    private static final String COL_IMP = "IMP";

    //cria a tabela e os campos a serem inseridos no banco
    private static final String SQL_TAB_AMEACAS = "create table " + TAB_AMEACAS + "(" + COL_ID + " integer primary key autoincrement," +
            COL_DESC + " text not null," + COL_END + " text not null," +  COL_IMP + " text not null)";

    private ameacasDBHelper dbHelper;
    private SQLiteDatabase db;
    private Context ctx;

    //dbhelper, para pegar as informações sobre o banco
    public AmeacasDBDAO(Context ctx){
        dbHelper = new ameacasDBHelper(ctx, NOME_BANCO, null, VERSAO_BANCO);
        this.ctx = ctx;
    }

    //abre o banco
    public void abrir(){
        db = dbHelper.getWritableDatabase();
    }

    //Fecha o banco
    public void fechar(){
        db.close();
    }

    //adiciona uma nova ameaca, inserindo ela no banco de acordo com o valor de cada campo
    public void adicionarAmeacas(Ameacas a){
        ContentValues values = new ContentValues();
        values.put(COL_DESC, a.getDesc());
        values.put(COL_END, a.getEnd());
        values.put(COL_IMP, a.getImp());
        db.insert(TAB_AMEACAS, null, values);
    }

    //recupera as ameacas cadatradas percorrendo por um array
    public List<Ameacas> recuperarAmeacas(){
        List<Ameacas> ameaca = new ArrayList<Ameacas>();
        Ameacas ameacas;
        String[] colunas = {COL_ID, COL_DESC, COL_END, COL_IMP};
        Cursor cursor = db.query(TAB_AMEACAS, colunas,null,null,null,null,COL_ID);
        if(cursor.getCount() == 0){
            return ameaca;
        }

        cursor.moveToFirst();
        do{
            ameacas = new Ameacas();
            ameacas.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            ameacas.setDesc(cursor.getString(cursor.getColumnIndex(COL_DESC)));
            ameacas.setEnd(cursor.getString(cursor.getColumnIndex(COL_END)));
            ameacas.setImp(cursor.getString(cursor.getColumnIndex(COL_IMP)));
            ameaca.add(ameacas);
        }while(cursor.moveToNext());
        return ameaca;
    }


    public Integer deletarAmeaca(int id){
        return db.delete(TAB_AMEACAS, COL_ID + "=" + id, null);
    }
    public Integer deletarAmeacas(){ return db.delete(TAB_AMEACAS, null, null);}

    private static class ameacasDBHelper extends SQLiteOpenHelper {

        public ameacasDBHelper(Context ctx, String desc, SQLiteDatabase.CursorFactory factory, int versao){
            super(ctx, desc, factory, versao);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_TAB_AMEACAS);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TAB_AMEACAS);
            onCreate(db);
        }
    }
}
