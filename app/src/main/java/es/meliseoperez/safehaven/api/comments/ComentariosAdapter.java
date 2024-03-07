package es.meliseoperez.safehaven.api.comments;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.meliseoperez.safehaven.R;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ViewHolder> {

    private List<Comentario> comentarioList;

    public ComentariosAdapter(List<Comentario> comentarioList){
        this.comentarioList = comentarioList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comentario comentario = comentarioList.get(position);
        holder.commentTextView.setText(comentario.getCommentText());
        // Configurar otros elementos de la vista como la imagen usando Glide o Picasso
    }

    @Override
    public int getItemCount() {
        Log.d("MIERDA: ", String.valueOf(comentarioList.size()));
        return comentarioList.size();
    }

    public void setComentariosList(List<Comentario> nuevosComentarios) {
       this.comentarioList = nuevosComentarios;
       notifyDataSetChanged();//Notifica que los datos han cambiado para refrescar la vista
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentTextView;
        //Declarar otros elementos de la vista
        public ViewHolder(View itemView){
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            //Inicializar otros elementos de la vista
        }
    }
}
