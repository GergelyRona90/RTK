package com.example.karesz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import java.io.File

class NewProject : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_project, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCreatePrj: Button = view.findViewById(R.id.btnCreateNewProject)
        val editTextCreateNewProject: EditText = view.findViewById(R.id.editTextCreateNewProject)
        btnCreatePrj.setOnClickListener {
            if (editTextCreateNewProject.text.isEmpty()) {
                Toast.makeText(context, "Kérlek ad meg a projektnevet!", Toast.LENGTH_LONG).show()

            } else {
                val newProjectFolderName: String = editTextCreateNewProject.text.toString()
                if (File(rtkFolder, newProjectFolderName).exists()) {
                    Toast.makeText(context, "Ilyen nevű projekt már létezik", Toast.LENGTH_LONG).show()
                } else {
                    File(rtkFolder,newProjectFolderName).mkdir()
                    Navigation.findNavController(view).navigate(R.id.action_newProject_to_projectBrowser)
                }
            }
        }
    }
}