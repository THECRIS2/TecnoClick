package com.ni.avalon.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ni.avalon.R;
import com.ni.avalon.model.MyCartModel;

import java.util.List;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.ViewHolder> {

    Context context;
    List<MyCartModel> cartModelList;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    public MyCartAdapter(Context context, List<MyCartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyCartModel currentItem = cartModelList.get(position);

        holder.nombre.setText(currentItem.getProductNombre());
        holder.precio.setText(currentItem.getProductPrecio());
        holder.fecha.setText(currentItem.getCurrentDate());
        holder.hora.setText(currentItem.getCurrentTime());
        holder.cantidad.setText(currentItem.getCantTotal());
        holder.precio_total.setText(currentItem.getPrecioTotal());

        // Configurar el botón para eliminar el ítem del carrito
        holder.eliminarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition(); // Usamos getAdapterPosition para obtener la posición actual del ViewHolder
                if (currentPosition != RecyclerView.NO_POSITION) {
                    MyCartModel itemToDelete = cartModelList.get(currentPosition);

                    firestore.collection("CurrentUser")
                            .document(auth.getCurrentUser().getUid())
                            .collection("AddToCart")
                            .document(itemToDelete.getDocumentid()) // Usar itemToDelete para obtener el ID del documento
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Eliminar el ítem de la lista y actualizar la vista
                                        cartModelList.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        notifyItemRangeChanged(currentPosition, cartModelList.size());
                                        Toast.makeText(context, "Item eliminado", Toast.LENGTH_SHORT).show();

                                        // Recalcular y enviar el nuevo total
                                        recalcularTotal();
                                    } else {
                                        Toast.makeText(context, "Error al eliminar este item", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        // Recalcular el total cada vez que se carga el RecyclerView
        recalcularTotal();
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    private void recalcularTotal() {
        int precio_total = 0;

        // Sumamos el precio total de cada ítem en la lista
        for (MyCartModel item : cartModelList) {
            try {
                int precioItem = Integer.parseInt(item.getPrecioTotal());
                precio_total += precioItem;
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Manejar el error en caso de que el precio no sea válido
            }
        }

        // Enviar el monto total actualizado como un Intent
        Intent intent = new Intent("MyTotalAmount");
        intent.putExtra("totalAmount", precio_total);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, precio, fecha, hora, cantidad, precio_total;
        ImageView eliminarItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.productName);
            precio = itemView.findViewById(R.id.productPrice);
            fecha = itemView.findViewById(R.id.productDate);
            hora = itemView.findViewById(R.id.productTime);
            cantidad = itemView.findViewById(R.id.productQuantity);
            precio_total = itemView.findViewById(R.id.productTotalPrice);
            eliminarItem = itemView.findViewById(R.id.delete_cart);
        }
    }
}