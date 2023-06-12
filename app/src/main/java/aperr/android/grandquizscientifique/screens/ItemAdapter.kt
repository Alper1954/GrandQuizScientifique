package aperr.android.grandquizscientifique.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import aperr.android.grandquizscientifique.R


class ItemAdapter(private val dataset: List<Category>, val callbackClickItem: (Int) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val catImage:ImageView = view.findViewById(R.id.catImage)
        val catName:TextView = view.findViewById(R.id.catName)
        val catItem:ConstraintLayout = view.findViewById(R.id.catItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]

        holder.catImage.setImageResource(item.picture)
        holder.catName.text = item.name
        holder.catItem.setBackgroundResource(item.background)
        holder.catItem.setOnClickListener {
            callbackClickItem(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = dataset.size
}