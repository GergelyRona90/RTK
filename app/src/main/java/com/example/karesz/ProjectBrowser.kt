package com.example.karesz

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.karesz.adapters.ProjectBrowserAdapter
import com.example.karesz.data.Datasource
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems


class ProjectBrowser : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_project_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_for_browser)
        myRecyclerView.layoutManager = LinearLayoutManager(context)
        // context miatt szükséges itt a requireContext()
        val projectFolders = Datasource().loadFolders(rtkFolder)

        val adapter = ProjectBrowserAdapter(
            requireContext(),
            projectFolders
        )
        myRecyclerView.adapter = adapter


        // oldalrahúzáshoz:
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val swipedProjectName = projectFolders[viewHolder.bindingAdapterPosition]
                val swipeProject = File(rtkFolder, swipedProjectName)
                val position = viewHolder.bindingAdapterPosition

                val archiveFolder = File(downloadDirectory, "RTK_Archive_Folder")
                checkFolderIsExist(archiveFolder)
            /*
                swipeProject.copyRecursively(archiveFolder, true)
                swipeProject.deleteRecursively()
             */
                val projectArchiveFolder = File(archiveFolder,swipedProjectName)
                checkFolderIsExist(projectArchiveFolder)
                val from = FileSystems.getDefault().getPath(swipeProject.absolutePath)
                val to = FileSystems.getDefault().getPath(archiveFolder.absolutePath)

                try {
                    if (swipeProject.copyRecursively(projectArchiveFolder,true)) {
                        Snackbar.make(myRecyclerView,"$swipedProjectName archiválva.",Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(myRecyclerView,"$swipedProjectName nem sikerült archiválni.",Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    println("Exception occurred while copying.")
                }
                swipeProject.deleteRecursively()

                projectFolders.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, projectFolders.size - position)

        //        Snackbar.make(myRecyclerView, swipedProjectName, Snackbar.LENGTH_LONG)
        //            .show()
                adapter.datas = projectFolders
                //     }
            }

            // elhúzás során megjelenjen a delete ikon és piros háttér:
            //forrás:
            // https://github.com/kitek/android-rv-swipe-delete/blob/master/app/src/main/java/pl/kitek/rvswipetodelete/SwipeToDeleteCallback.kt
            // https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                // dX > 0 Right Swipe
                // dX < 0 Left Swpie
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                //if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView


                val archiveIcon: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.send_to_archive_icon)
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicHeight = archiveIcon?.intrinsicHeight
                val intrinsicWidth = archiveIcon?.intrinsicWidth
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                var deleteIconLeft: Int = 0
                var deleteIconRight: Int = 0
                var deleteIconBottom: Int = 0
                if (dX < 0) {
                    val background =
                        ContextCompat.getDrawable(requireContext(),
                            R.drawable.archiveitem_background
                        )
                    background?.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background?.draw(c)
                    deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!!
                    deleteIconRight = itemView.right - deleteIconMargin
                    deleteIconBottom = deleteIconTop + intrinsicHeight


                } else if (dX > 0) {
                    val background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.archiveitem_background)
                    background?.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background?.draw(c)
                    deleteIconLeft = itemView.left + deleteIconMargin
                    deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth!!
                    deleteIconBottom = deleteIconTop + intrinsicHeight
                }
                archiveIcon.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                archiveIcon.draw(c)
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }


        }).attachToRecyclerView(myRecyclerView)
    }

    fun moveFolder(from: File, to: File) {
        if (!from.exists() || !from.isDirectory) {
            // A forrásmappa nem létezik vagy nem mappa
            return
        }

        if (!to.exists() || !to.isDirectory) {
            // A célmappa nem létezik vagy nem mappa
            return
        }

        try {
            from.copyRecursively(to, true)
            from.deleteRecursively()
        } catch (ex: IOException) {
            ex.printStackTrace()
            // Hiba történt az áthelyezés közben
        }
    }
}




