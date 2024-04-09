package es.meliseoperez.safehaven.api.comments;

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

/**
 * Adaptador para el RecyclerView que muestra comentarios.
 * Convierte cada objeto Comentario en elementos de la vista que pueden ser agregados al RecyclerView.
 */
public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ViewHolder> {

    private List<Comentario> comentarioList;
    private OnComentarioClickListener listener;

    public ComentariosAdapter(List<Comentario> comentarioList, OnComentarioClickListener listener) {
        this.comentarioList = comentarioList;
        this.listener = listener;
    }

    public ComentariosAdapter(List<Comentario> comentarios) {
        this.comentarioList = comentarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario comentario = comentarioList.get(position);
        holder.commentTextView.setText(comentario.getCommentText());
        // Uso de Glide para la carga eficiente de imágenes desde una URL.
        Glide.with(holder.itemView.getContext())
                .load(comentario.getImageUrl())
                .into(holder.commentImageView);
    }

    @Override
    public int getItemCount() {
        return comentarioList.size();
    }

    public void setComentariosList(List<Comentario> nuevosComentarios) {
        this.comentarioList = nuevosComentarios;
        notifyDataSetChanged(); // Notifica cambios para actualizar la vista.
    }

    /**
     * ViewHolder para elementos de comentario.
     * Incluye un TextView para el texto del comentario y un ImageView para una imagen asociada, si existe.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentTextView;
        public ImageView commentImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentImageView = itemView.findViewById(R.id.commentImageView);

            itemView.setOnClickListener(view -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onComentarioClick(comentarioList.get(getAdapterPosition()));
                }
            });
        }
    }

    /**
     * Interfaz para manejar clics en elementos de la lista de comentarios.
     */
    public interface OnComentarioClickListener {
        void onComentarioClick(Comentario comentario);
    }
}
