package br.feevale.ameacasambientais;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.*;
import java.util.List;

/**
 * Created by 0126096 on 01/11/2016.
 */
public class riscoAmbiental extends BaseAdapter {

    private List<Ameacas> ameaca;
    private AmeacasAmbientais ctx;
    AmeacasDBDAO db;

    //cria a recuperacao das informacoes
    public riscoAmbiental(AmeacasAmbientais ctx){
        this.ctx = ctx;
        db = ctx.db;
        ameaca = db.recuperarAmeacas();
    }

    //recupera a posição do item a ser buscado
    @Override
    public int getCount() {
        ameaca = db.recuperarAmeacas();
        return ameaca.size();
    }

    @Override
    public Object getItem(int position) {
        ameaca = db.recuperarAmeacas();
        return ameaca.get(position);
    }

    @Override
    public long getItemId(int position) {
        ameaca = db.recuperarAmeacas();
        if(ameaca.size() == 0){
            return 0;
        }
        return ameaca.get(position).getId();
    }

    //retorna a descrição da ameaça e junto com ele o seu impacto ambiental
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ameaca = db.recuperarAmeacas();
        LayoutInflater inflater = ctx.getLayoutInflater();
        View row;
        if(position % 2 == 0){
            row = inflater.inflate(R.layout.list_ameacas_item, parent, false);
        }else{
            row = inflater.inflate(R.layout.list_ameacas_item_left, parent, false);
        }

        TextView tvDes = (TextView)row.findViewById(R.id.txtDesc);
        TextView tvEnd = (TextView)row.findViewById(R.id.txtEnd);
        TextView tvImp = (TextView)row.findViewById(R.id.txtImp);
        tvDes.setText(ameaca.get(position).getDesc());
        tvEnd.setText(ameaca.get(position).getEnd());
        tvImp.setText(ameaca.get(position).getImp());
        return row;
    }
}
