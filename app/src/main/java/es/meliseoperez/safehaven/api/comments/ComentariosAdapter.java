package es.meliseoperez.safehaven.api.comments;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import es.meliseoperez.safehaven.R;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ViewHolder> {

    private List<Comentario> comentarioList;
    private OnComentarioClickListener listener;

    public ComentariosAdapter(List<Comentario> comentarioList, OnComentarioClickListener listener){
        this.comentarioList = comentarioList;
        this.listener = listener;
    }

    public ComentariosAdapter(List<Comentario> comentarios) {
        this.comentarioList = comentarios;
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
        // Cargar la imagen usando Glide
        Glide.with(holder.itemView.getContext())
                .load(comentario.getImageUrl()) // Asegúrate de que tu clase Comentario tenga un getter para imageUrl
                .into(holder.commentImageView);
    }

    @Override
    public int getItemCount() {
        Log.d("MIERDA: ", "VALOR DE GETITEMCOUNT: " + String.valueOf(comentarioList.size()));
        return comentarioList.size();
    }

    public void setComentariosList(List<Comentario> nuevosComentarios) {
       this.comentarioList = nuevosComentarios;
       notifyDataSetChanged();//Notifica que los datos han cambiado para refrescar la vista
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentTextView;
        public ImageView commentImageView;
        //Declarar otros elementos de la vista
        public ViewHolder(View itemView){
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentImageView = itemView.findViewById(R.id.commentImageView);
            //Inicializar otros elementos de la vista
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null && getAdapterPosition() != RecyclerView.NO_POSITION){
                        Comentario comentario = comentarioList.get(getAdapterPosition());
                        Log.d("ComentarioClick", "Comentario ID: " + comentario.getId());
                        listener.onComentarioClick(comentarioList.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
    public interface OnComentarioClickListener{
        void onComentarioClick(Comentario comentario);
    }
}
