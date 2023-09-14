package com.example.karesz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.karesz.adapters.ViewPager2AdapterForRTKImages
import com.example.karesz.data.Datasource
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator
import java.io.File

// itt jelenek meg a projekt képei úgy, hogy oldalrahúzva tudunk majd lapozni
class RTKImage3 : Fragment() {
    private val args: RTKImage3Args by navArgs()

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
        return inflater.inflate(R.layout.fragment_rtk_image3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = args.projectName




        // deklaráljuk a viewPager-t ami felelős a megfelelő lapozásért (olyan, mint a RecyclerView
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerForRTKImages)

        // képek és azok neveinek betöltése az adapter számára
        // az adapter ViewPager2AdapterForRTKImages.kt-ben található meg
        val f = File(rtkFolder, args.projectName)
        val jpgNames = Datasource().loadJPGFilesNames(f)
        val jpgFiles = Datasource().loadJPGFiles(f)
        val adapter = ViewPager2AdapterForRTKImages(
            jpgNames,
            jpgFiles
        )
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.setCurrentItem(args.pos,false)



        //pontok deklarálása, ami a képek számíitól függ
        //  val indicator =view.findViewById<CircleIndicator3>(R.id.indicator)
        val indicator = view.findViewById<ScrollingPagerIndicator>(R.id.indicator)
        //  indicator.setViewPager(viewPager)
        indicator.attachToPager(viewPager)

    }
}