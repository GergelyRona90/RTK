package com.example.karesz

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.karesz.adapters.ProjectAdapter
import com.example.karesz.data.Datasource
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Project : Fragment() {
    private val args: ProjectArgs by navArgs()
    private lateinit var recyclerViewForImages: RecyclerView
    private var pos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_project, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnToMap = view.findViewById<FloatingActionButton>(R.id.btnToMap)
        // megkeressük a button-t, amivel fotót készítő fragmentbe ugrunk majd
        val btnToNewPhoto = view.findViewById<FloatingActionButton>(R.id.btnToNewPhoto)
        //args-ből lementjük a projekt nevét
        val projectName = args.projectName
        // a MainActivity-ben deklarált rtkFolder és az áthozott projektnév segítségével definiáljuk
        // a projekt mappát
        val projectFolder = File(rtkFolder, projectName)
        // megkersük a recyclerview-t, amiben szerepelni fognak az adott projektnek a képei, mellette
        // pedig a kép nevei
        /*   val recyclerViewForImages: RecyclerView =
               view.findViewById(R.id.recycler_view_for_project)
          */
        recyclerViewForImages = view.findViewById(R.id.recycler_view_for_project)
        // definiáljuk az adaptert (ProjectAdapter.kt)
        // összegyűjtjük az adapterhez a listát (a képeket és azok nevüket)
        val namesOfImages = Datasource().loadJPGFilesNames(projectFolder)
        val listOfImages = Datasource().loadJPGFiles(projectFolder)
        // adapter definiálása RecyclerView-hoz
        val adapter = ProjectAdapter(
            namesOfImages,
            listOfImages,
            requireContext(),
            args.projectName,
        )

        // a fejléc neve a projekt neve lesz:
        (activity as MainActivity).supportActionBar?.title = projectName
        // recyclerview megjelenítése
        recyclerViewForImages.adapter = adapter
        recyclerViewForImages.layoutManager = LinearLayoutManager(requireContext())
        val layoutManager= LinearLayoutManager(requireActivity())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerViewForImages.layoutManager  = layoutManager

        // létrehozzuk az action-t, amivel átmegyünk a kamerakészítő fragmentbe
        // és visszük a projekt nevét is
        // ellenőrizzük, hogy melyik az utolsó kép neve:

        val action = ProjectDirections.actionProjectFolderToCameraReview(
            projectName
        )
      //  val action2 = ProjectDirections.actionProjectToDisplayRTKImagesInMap(projectName)
        val action2 = ProjectDirections.actionProjectToOSMMap(projectName)
        val action3 = ProjectDirections.actionProjectToOSMMap(projectName)

        btnToNewPhoto.setOnClickListener {
            view.findNavController().navigate(action)
        }
        btnToMap.setOnClickListener {
            view.findNavController().navigate(action3)
        }


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
                val swipedImageName = namesOfImages[viewHolder.bindingAdapterPosition]
                val swipeImage = File(projectFolder, swipedImageName)
                val position = viewHolder.bindingAdapterPosition

                val deletedImageFolder = File(downloadDirectory, "DeletedImages")

            //    checkFolderIsExist(deletedImageFolder)

             //   swipeImage.copyTo(deletedImageFolder,true)
                copyFileToFolder(swipeImage,deletedImageFolder)

                swipeImage.delete()
                listOfImages.removeAt(position)
                namesOfImages.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, namesOfImages.size - position)

                Snackbar.make(recyclerViewForImages, swipedImageName, Snackbar.LENGTH_LONG)
                    .show()
                adapter.namesOfImages = namesOfImages
                adapter.listOfImages = listOfImages
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

                val deleteIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.delete_icon)
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicHeight = deleteIcon?.intrinsicHeight
                val intrinsicWidth = deleteIcon?.intrinsicWidth
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                var deleteIconLeft: Int = 0
                var deleteIconRight: Int = 0
                var deleteIconBottom: Int = 0
                if (dX < 0) {
                    val background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.deleteitem_background
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
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.deleteitem_background
                        )
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
                deleteIcon.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteIcon.draw(c)
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


        }).attachToRecyclerView(recyclerViewForImages)
    }
fun copyFileToFolder(sourceFile: File, destinationFolder: File) {
        val sourcePath: Path = Paths.get(sourceFile.absolutePath)
        val destinationPath: Path = Paths.get(destinationFolder.absolutePath, sourceFile.name)

        Files.copy(sourcePath, destinationPath)
    }
}


