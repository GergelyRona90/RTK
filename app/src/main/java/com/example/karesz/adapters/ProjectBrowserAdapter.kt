package com.example.karesz.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.karesz.ProjectBrowserDirections
import com.example.karesz.R
import com.example.karesz.data.Datasource
import com.example.karesz.rtkFolder
import java.io.File

// adapter a projekteket tartalmozó mappák megjelenítésére
class ProjectBrowserAdapter(private val context: Context, var datas: List<String>) :
    RecyclerView.Adapter<ProjectBrowserAdapter.ItemViewHolder>() {

    init {
        val sortedList = datas.sorted()
        datas = sortedList
    }

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textViewForProjectNames = view.findViewById<TextView>(R.id.projectNames)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.textviewforbrowser, parent, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = datas.get(position)
        holder.textViewForProjectNames.text = item
        holder.textViewForProjectNames.setOnClickListener {
            val action = ProjectBrowserDirections.actionProjectBrowserToProject(projectName = item)
            holder.view.findNavController().navigate(action)
        }
        holder.textViewForProjectNames.setOnLongClickListener {
            val oldFolderName = item
            val oldFolderFile = File(rtkFolder, oldFolderName)
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val dialogLayout = inflater.inflate(R.layout.edit_text_for_change_image_name, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editTextForChangeImageName)
            builder.setTitle("Project Átnevezése")
            builder.setPositiveButton("Igen") { dialog, _ ->
                val newFolderName = editText.text.toString()
                val newImageFolder = File(rtkFolder.toString(), newFolderName)
                oldFolderFile.renameTo(newImageFolder)
                val tmpSortedList = datas.sorted()
                datas = Datasource().loadFolders(rtkFolder)
                notifyDataSetChanged()
            }
            builder.setNegativeButton("Nem") { dialog, _ -> }
            builder.setView(dialogLayout)
            builder.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }
}


