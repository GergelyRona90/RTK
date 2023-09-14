package com.example.karesz.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.karesz.R
//Adapter
// a projekt képeire rányomunk, akkor oldalrahúzás során tudjunk lapozni
class ViewPager2AdapterForRTKImages(
    //kellenek a képek nevei megjelenítésre
    private var namesOfImages: List<String>,
    //Uri, hogy megtaláljuk a meghajtón a képeket
    private var listOfImages: List<Uri>,
) : RecyclerView.Adapter<ViewPager2AdapterForRTKImages.Pager2ViewHolder>()
    {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //fragment_rtk_image-ben található a layout, amely a nagyobb kép és annak neveinek megjelnítésére
        // hoztam létre
        val itemNameOfImages: TextView = itemView.findViewById(R.id.textViewForBiggerImage)
        val itemImage: ImageView = itemView.findViewById(R.id.imageViewForBiggerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        val layout: View = LayoutInflater
            .from(parent.context)
                //azt a layout-t kell itt inflatel-nünk, ahol található a nagyobb képek, valamint a hozzá-
                //tartozó nevek
            .inflate(R.layout.fragment_rtk_image, parent, false)
        return Pager2ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        //beállítjuk a megfelelő neveket és a képek elérhetőségét
        val uriImage = listOfImages.get(position)
        holder.itemImage.setImageURI(uriImage)
        holder.itemNameOfImages.text = namesOfImages[position]

    }

    override fun getItemCount(): Int {
        return namesOfImages.size
    }
}