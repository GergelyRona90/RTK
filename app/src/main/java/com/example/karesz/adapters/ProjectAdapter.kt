package com.example.karesz.adapters

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.karesz.ProjectDirections
import com.example.karesz.R
import com.example.karesz.data.Datasource
import com.example.karesz.rtkFolder
import java.io.File


// adapter az egy projekt-hez tartozó képek megjelenítésére
class ProjectAdapter(
    var namesOfImages: List<String>,
    var listOfImages: List<Uri>,
    private val context: Context,
    val projectName: String,
) : RecyclerView.Adapter<ProjectAdapter.ItemViewHolder>() {

    init {
        val tmpSortedListOfNamesOfImages = namesOfImages.sorted()
        namesOfImages = tmpSortedListOfNamesOfImages
        val tmpSortedListOfImages = listOfImages.sorted()
        listOfImages = tmpSortedListOfImages
    }

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textViewForProject: TextView = view.findViewById(R.id.textOfImageOfRTK)
        val imageViewForProject: ImageView = view.findViewById(R.id.imageOfImageOfRTK)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.views_for_one_project, parent, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = namesOfImages.get(position)
        holder.textViewForProject.text = item
        holder.imageViewForProject.setImageURI(listOfImages.get(position))
        val uriImage = listOfImages.get(position)

        val action =
            ProjectDirections.actionProjectToRTKImage3(projectName, position)

        val animation =
            AnimationUtils.loadAnimation(context, R.anim.scale_animation)
        holder.view.startAnimation(animation)

        holder.textViewForProject.setOnClickListener {
            holder.view.findNavController().navigate(action)
        }
        holder.imageViewForProject.setOnClickListener {
            holder.view.findNavController().navigate(action)
        }
        holder.itemView.setOnClickListener {
            holder.view.findNavController().navigate(action)
        }


        holder.imageViewForProject.setOnLongClickListener {
            renameImage(item)
            true
        }

        holder.textViewForProject.setOnLongClickListener {
            renameImage(item)
            true
        }
    }

    override fun getItemCount(): Int {
        return namesOfImages.size
    }

    // kép átnevezése, ha hosszan nyomjuk a képet, vagy annak a nevét
    private fun renameImage(oldImageName: String) {
        val oldImageFile = File(rtkFolder, "$projectName/$oldImageName")
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout =
            inflater.inflate(R.layout.edit_text_for_change_image_name, null)
        val editText =
            dialogLayout.findViewById<EditText>(R.id.editTextForChangeImageName)
        builder.setTitle("Kép átnevezése")
        builder.setPositiveButton("Átnevezés") { dialog, _ ->
            val newImageName = editText.text.toString() + ".jpg"
            val newImageFile = File(rtkFolder, "$projectName/$newImageName")
            oldImageFile.renameTo(newImageFile)
            val projectFolder = File(rtkFolder, projectName)

            namesOfImages = Datasource().loadJPGFilesNames(projectFolder).sorted()
            listOfImages = Datasource().loadJPGFiles(projectFolder).sorted()
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Mégsem") { dialog, _ ->
        }
        builder.setView(dialogLayout)
        builder.show()
    }


}
